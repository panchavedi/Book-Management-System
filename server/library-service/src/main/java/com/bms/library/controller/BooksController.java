package com.bms.library.controller;

import com.bms.library.dto.*;
import com.bms.library.service.BookService;
import com.bms.library.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BooksController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> findAll() {

        return ResponseEntity.ok(
                bookService.findAll()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> search(
            @RequestParam String keyword
    ) {

        return ResponseEntity.ok(
                bookService.search(keyword)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                bookService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {

        return ResponseEntity.ok(
                bookService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        bookService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<BorrowingResponse> borrow(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                bookService.borrow(id, borrowerId)
        );
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowingResponse> returnBook(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                bookService.returnBook(id, borrowerId)
        );
    }
}
