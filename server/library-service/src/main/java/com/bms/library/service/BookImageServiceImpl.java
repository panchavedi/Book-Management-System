package com.bms.library.service;

import com.bms.library.dto.BookImageResponse;
import com.bms.library.entity.Book;
import com.bms.library.entity.BookImage;
import com.bms.library.exception.BookNotFoundException;
import com.bms.library.image.BookImageStorageService;
import com.bms.library.repository.BookImageRepository;
import com.bms.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookImageServiceImpl implements BookImageService {

    public static final int MAX_IMAGES_PER_BOOK = 5;

    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final BookImageStorageService storageService;

    @Value("${book.images.public-base-path:/books}")
    private String publicBasePath;

    @Override
    @Transactional
    public List<BookImageResponse> addImages(Long bookId, List<MultipartFile> images) {
        Book book = getBook(bookId);
        List<MultipartFile> normalized = normalize(images);

        long existingCount = bookImageRepository.countByBookId(bookId);
        if (existingCount + normalized.size() > MAX_IMAGES_PER_BOOK) {
            throw new IllegalArgumentException(
                    "A book can have a maximum of " + MAX_IMAGES_PER_BOOK + " images"
            );
        }

        return saveImages(book, normalized, (int) existingCount);
    }

    @Override
    @Transactional
    public List<BookImageResponse> replaceImages(Long bookId, List<MultipartFile> images) {
        Book book = getBook(bookId);
        List<MultipartFile> normalized = normalize(images);

        List<BookImage> existing = bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId);
        List<String> oldStorageKeys = existing.stream()
                .map(BookImage::getStorageKey)
                .toList();
        List<String> newStorageKeys = new ArrayList<>();
        List<BookImage> newEntities = new ArrayList<>();

        try {
            for (int i = 0; i < normalized.size(); i++) {
                MultipartFile source = normalized.get(i);
                BookImageStorageService.StoredBookImage stored = storageService.store(bookId, source);
                newStorageKeys.add(stored.storageKey());
                newEntities.add(buildEntity(book, stored, source, i));
            }

            bookImageRepository.deleteByBookId(bookId);
            List<BookImage> persisted = bookImageRepository.saveAll(newEntities);
            bookImageRepository.flush();
            finalizeUrls(persisted);

            registerFileLifecycle(newStorageKeys, oldStorageKeys);

            return persisted.stream().map(this::toResponse).toList();
        } catch (RuntimeException | IOException ex) {
            newStorageKeys.forEach(storageService::delete);
            throw wrap(ex);
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long bookId, Long imageId) {
        getBook(bookId);

        BookImage image = bookImageRepository.findByIdAndBookId(imageId, bookId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book image " + imageId + " was not found for book " + bookId
                ));

        String storageKey = image.getStorageKey();
        bookImageRepository.delete(image);
        bookImageRepository.flush();
        registerFileLifecycle(List.of(), List.of(storageKey));

        List<BookImage> remaining = bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDisplayOrder(i);
        }
        bookImageRepository.saveAll(remaining);
    }

    @Override
    @Transactional(readOnly = true)
    public StoredImage getImage(Long bookId, Long imageId) {
        getBook(bookId);

        BookImage image = bookImageRepository.findByIdAndBookId(imageId, bookId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book image " + imageId + " was not found for book " + bookId
                ));

        Resource resource = storageService.load(image.getStorageKey());
        return new StoredImage(resource, image.getContentType(), image.getOriginalFileName());
    }

    @Override
    @Transactional
    public void scheduleDeleteForBook(Long bookId) {
        getBook(bookId);
        List<BookImage> images = bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId);
        List<String> storageKeys = images.stream()
                .map(BookImage::getStorageKey)
                .toList();
        bookImageRepository.deleteByBookId(bookId);
        bookImageRepository.flush();
        registerFileLifecycle(List.of(), storageKeys);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookImageResponse> findImages(Long bookId) {
        getBook(bookId);
        return bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void registerFileLifecycle(List<String> newStorageKeys, List<String> oldStorageKeys) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            oldStorageKeys.forEach(storageService::delete);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        oldStorageKeys.forEach(storageService::delete);
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            newStorageKeys.forEach(storageService::delete);
                        }
                    }
                }
        );
    }

    private Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private List<MultipartFile> normalize(List<MultipartFile> images) {
        if (images == null) {
            return List.of();
        }

        List<MultipartFile> normalized = images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .toList();

        if (normalized.size() > MAX_IMAGES_PER_BOOK) {
            throw new IllegalArgumentException(
                    "A book can have a maximum of " + MAX_IMAGES_PER_BOOK + " images"
            );
        }

        return normalized;
    }

    private List<BookImageResponse> saveImages(
            Book book,
            List<MultipartFile> images,
            int startOrder
    ) {
        List<BookImage> entities = new ArrayList<>();
        List<String> storageKeys = new ArrayList<>();

        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile source = images.get(i);
                BookImageStorageService.StoredBookImage stored = storageService.store(book.getId(), source);
                storageKeys.add(stored.storageKey());
                entities.add(buildEntity(book, stored, source, startOrder + i));
            }

            List<BookImage> persisted = bookImageRepository.saveAll(entities);
            bookImageRepository.flush();
            finalizeUrls(persisted);
            registerFileLifecycle(storageKeys, List.of());
            return persisted.stream().map(this::toResponse).toList();
        } catch (RuntimeException | IOException ex) {
            storageKeys.forEach(storageService::delete);
            throw wrap(ex);
        }
    }

    private void finalizeUrls(List<BookImage> images) {
        for (BookImage image : images) {
            image.setImageUrl(publicBasePath.replaceAll("/$", "")
                    + "/" + image.getBook().getId()
                    + "/images/" + image.getId());
        }
        bookImageRepository.saveAll(images);
        bookImageRepository.flush();
    }

    private BookImage buildEntity(
            Book book,
            BookImageStorageService.StoredBookImage stored,
            MultipartFile source,
            int displayOrder
    ) {
        return BookImage.builder()
                .book(book)
                .storageKey(stored.storageKey())
                .imageUrl(publicBasePath.replaceAll("/$", "")
                        + "/" + book.getId() + "/images/{imageId}")
                .originalFileName(StringUtils.cleanPath(
                        source.getOriginalFilename() == null ? "image" : source.getOriginalFilename()
                ))
                .contentType(stored.contentType())
                .fileSize(stored.fileSize())
                .displayOrder(displayOrder)
                .build();
    }

    private BookImageResponse toResponse(BookImage image) {
        return BookImageResponse.builder()
                .id(image.getId())
                .url(image.getImageUrl().replace("{imageId}", String.valueOf(image.getId())))
                .originalFileName(image.getOriginalFileName())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    private RuntimeException wrap(Exception ex) {
        if (ex instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new IllegalStateException("Unable to store book image", ex);
    }
}
