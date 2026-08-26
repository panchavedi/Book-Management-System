package com.bms.library.service;

import com.bms.library.dto.*;
import com.bms.library.entity.*;
import com.bms.library.exception.*;
import com.bms.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowingRepository borrowingRepository;

    @Override
    @Transactional
    public BookResponse create(BookRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException(
                    "A book with ISBN " +
                            request.getIsbn() +
                            " already exists"
            );
        }

        Author author =
                authorRepository.findById(request.getAuthorId())
                        .orElseThrow(
                                () -> new AuthorNotFoundException(
                                        request.getAuthorId()
                                )
                        );

        Category category =
                categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        request.getCategoryId()
                                )
                        );

        Book book = Book.builder()
                .title(request.getTitle().trim())
                .isbn(request.getIsbn().trim())
                .author(author)
                .category(category)
                .publisher(request.getPublisher())
                .printedOn(request.getPrintedOn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .about(request.getAbout())
                .build();

        return toResponse(
                bookRepository.save(book)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {

        return bookRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse findById(
            Long bookId,
            Long borrowerId
    ) {

        Book book =
                bookRepository
                        .findWithAuthorAndCategoryById(bookId)
                        .orElseThrow(
                                () -> new BookNotFoundException(bookId)
                        );

        boolean borrowed =
                borrowingRepository
                        .existsByBookIdAndBorrowerIdAndStatus(
                                bookId,
                                borrowerId,
                                BorrowingStatus.BORROWED
                        );

        return toResponse(book, borrowed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findByAuthor(
            Long authorId
    ) {
        if (!authorRepository.existsById(authorId)) {
            throw new AuthorNotFoundException(authorId);
        }

        return bookRepository
                .findByAuthorId(authorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findByCategory(
            Long categoryId
    ) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException(categoryId);
        }

        return bookRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookResponse update(
            Long id,
            BookRequest request
    ) {

        Book book =
                bookRepository
                        .findByIdForUpdate(id)
                        .orElseThrow(
                                () -> new BookNotFoundException(id)
                        );

        if (bookRepository.existsByIsbnAndIdNot(
                request.getIsbn(),
                id
        )) {
            throw new IllegalArgumentException(
                    "A book with ISBN " +
                            request.getIsbn() +
                            " already exists"
            );
        }

        Author author =
                authorRepository.findById(request.getAuthorId())
                        .orElseThrow(
                                () -> new AuthorNotFoundException(
                                        request.getAuthorId()
                                )
                        );

        Category category =
                categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        request.getCategoryId()
                                )
                        );

        int currentlyBorrowed =
                book.getBorrowedCopies();

        if (request.getTotalCopies() < currentlyBorrowed) {

            throw new IllegalArgumentException(
                    "Total copies cannot be less than " +
                            "currently borrowed copies: " +
                            currentlyBorrowed
            );
        }

        book.setTitle(request.getTitle().trim());
        book.setIsbn(request.getIsbn().trim());
        book.setAuthor(author);
        book.setCategory(category);
        book.setPublisher(request.getPublisher());
        book.setPrintedOn(request.getPrintedOn());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(
                request.getTotalCopies() -
                        currentlyBorrowed
        );
        book.setAbout(request.getAbout());

        return toResponse(book);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Book book =
                bookRepository
                        .findByIdForUpdate(id)
                        .orElseThrow(
                                () -> new BookNotFoundException(id)
                        );

        boolean hasActiveBorrowings =
                borrowingRepository
                        .existsByBookIdAndStatus(
                                id,
                                BorrowingStatus.BORROWED
                        );

        if (hasActiveBorrowings) {
            throw new IllegalStateException(
                    "Book cannot be deleted while it has active borrowers"
            );
        }

        bookRepository.delete(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> search(String keyword) {

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return findAll();
        }

        return bookRepository
                .search(keyword.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BorrowingResponse borrow(
            Long bookId,
            Long borrowerId
    ) {

        /*
         * PESSIMISTIC_WRITE locks the book row.
         *
         * Another borrow transaction for the same book
         * must wait until this transaction commits/rolls back.
         */
        Book book =
                bookRepository
                        .findByIdForUpdate(bookId)
                        .orElseThrow(
                                () -> new BookNotFoundException(bookId)
                        );

        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException(bookId);
        }

        boolean alreadyBorrowed =
                borrowingRepository
                        .existsByBookIdAndBorrowerIdAndStatus(
                                bookId,
                                borrowerId,
                                BorrowingStatus.BORROWED
                        );

        if (alreadyBorrowed) {
            throw new ActiveBorrowingExistsException(
                    bookId,
                    borrowerId
            );
        }

        book.borrowCopy();

        Borrowing borrowing =
                Borrowing.builder()
                        .book(book)
                        .borrowerId(borrowerId)
                        .borrowedOn(LocalDateTime.now())
                        .status(BorrowingStatus.BORROWED)
                        .build();

        Borrowing saved =
                borrowingRepository.save(borrowing);

        return toBorrowingResponse(saved);
    }

    @Override
    @Transactional
    public BorrowingResponse returnBook(
            Long bookId,
            Long borrowerId
    ) {

        Book book =
                bookRepository
                        .findByIdForUpdate(bookId)
                        .orElseThrow(
                                () -> new BookNotFoundException(bookId)
                        );

        Borrowing borrowing =
                borrowingRepository
                        .findActiveBorrowingForUpdate(
                                bookId,
                                borrowerId
                        )
                        .orElseThrow(
                                () -> new BorrowingNotFoundException(
                                        bookId,
                                        borrowerId
                                )
                        );

        borrowing.markReturned();

        book.returnCopy();

        return toBorrowingResponse(borrowing);
    }

    private BookResponse toResponse(Book book) {

        return toResponse(book, false);
    }

    private BookResponse toResponse(
            Book book,
            boolean borrowed
    ) {

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())
                .categoryId(book.getCategory().getId())
                .categoryName(book.getCategory().getName())
                .publisher(book.getPublisher())
                .printedOn(book.getPrintedOn())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .borrowedCopies(book.getBorrowedCopies())
                .about(book.getAbout())
                .borrowed(borrowed)
                .build();
    }

    private BorrowingResponse toBorrowingResponse(
            Borrowing borrowing
    ) {

        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .bookId(borrowing.getBook().getId())
                .bookTitle(borrowing.getBook().getTitle())
                .borrowerId(borrowing.getBorrowerId())
                .borrowedOn(borrowing.getBorrowedOn())
                .returnedOn(borrowing.getReturnedOn())
                .status(borrowing.getStatus())
                .build();
    }
}
