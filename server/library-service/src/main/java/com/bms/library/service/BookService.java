package com.bms.library.service;

import com.bms.library.dto.*;
import java.util.List;

public interface BookService {

    BookResponse create(BookRequest request);

    List<BookResponse> findAll();

    BookResponse findById(Long id, Long borrowerId);

    List<BookResponse> findByAuthor(
            Long authorId
    );

    List<BookResponse> findByCategory(
            Long categoryId
    );

    List<BookResponse> search(String keyword);

    BookResponse update(
            Long id,
            BookRequest request
    );

    void delete(Long id);

    BorrowingResponse borrow(
            Long bookId,
            Long borrowerId
    );

    BorrowingResponse returnBook(
            Long bookId,
            Long borrowerId
    );
}
