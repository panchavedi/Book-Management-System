package com.bms.library.image;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileSystemBookImageStorageService implements BookImageStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    private static final long MAX_IMAGE_PIXELS = 25_000_000L;

    private final Path rootDirectory;
    private final long maxFileSizeBytes;

    public FileSystemBookImageStorageService(
            @Value("${book.images.storage-directory:./uploads/books}") String storageDirectory,
            @Value("${book.images.max-file-size-bytes:10485760}") long maxFileSizeBytes
    ) {
        this.rootDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @PostConstruct
    void initialize() throws IOException {
        Files.createDirectories(rootDirectory);
    }

    @Override
    public StoredBookImage store(Long bookId, MultipartFile file) throws IOException {
        validate(file);

        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String extension = extensionFor(contentType);
        String fileName = UUID.randomUUID() + extension;
        String storageKey = bookId + "/" + fileName;

        Path bookDirectory = rootDirectory.resolve(String.valueOf(bookId)).normalize();
        if (!bookDirectory.startsWith(rootDirectory)) {
            throw new IOException("Invalid image storage path");
        }
        Files.createDirectories(bookDirectory);

        Path target = bookDirectory.resolve(fileName).normalize();
        if (!target.startsWith(bookDirectory)) {
            throw new IOException("Invalid image file path");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Files.deleteIfExists(target);
            throw ex;
        }

        return new StoredBookImage(storageKey, contentType, file.getSize());
    }

    @Override
    public Resource load(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            throw new IllegalArgumentException("Image storage key is required");
        }

        Path target = rootDirectory.resolve(storageKey).normalize();
        if (!target.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Invalid image storage key");
        }

        Resource resource = new FileSystemResource(target);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Book image file is unavailable");
        }
        return resource;
    }

    @Override
    public void delete(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }

        try {
            Path target = rootDirectory.resolve(storageKey).normalize();
            if (!target.startsWith(rootDirectory)) {
                log.warn("Skipped deletion of unsafe image storage key: {}", storageKey);
                return;
            }
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            log.warn("Unable to delete book image file {}", storageKey, ex);
        }
    }

    private void validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Each book image must contain a file");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException(
                    "Book image exceeds the maximum allowed size of "
                            + maxFileSizeBytes + " bytes"
            );
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)
                || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Unsupported image type. Allowed types: JPEG, PNG and GIF"
            );
        }

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new IllegalArgumentException("Uploaded file is not a valid image");
            }

            long pixels = (long) image.getWidth() * image.getHeight();
            if (pixels > MAX_IMAGE_PIXELS) {
                throw new IllegalArgumentException(
                        "Book image dimensions are too large. Maximum supported pixels: "
                                + MAX_IMAGE_PIXELS
                );
            }
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("Unsupported image type");
        };
    }
}
