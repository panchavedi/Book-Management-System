package com.bms.library.service;

import com.bms.library.dto.*;
import java.util.List;

public interface BookService {

    BookResponse create(BookRequest request);

    List<BookResponse> findAll();

    BookResponse findById(Long id);

    BookResponse update(
            Long id,
            BookRequest request
    );

    void delete(Long id);

    List<BookResponse> search(String keyword);

    BorrowingResponse borrow(
            Long bookId,
            Long borrowerId
    );

    BorrowingResponse returnBook(
            Long bookId,
            Long borrowerId
    );
}
