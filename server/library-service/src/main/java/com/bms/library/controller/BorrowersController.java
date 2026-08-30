package com.bms.library.controller;

import com.bms.library.dto.BorrowingResponse;
import com.bms.library.security.CurrentUser;
import com.bms.library.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowersController {

    private final BorrowingService borrowingService;

    @GetMapping("/me")
    public ResponseEntity<List<BorrowingResponse>> findMyBorrowingHistory(
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                borrowingService.findMyBorrowingHistory(borrowerId)
        );
    }

    @GetMapping("/me/books")
    public ResponseEntity<List<BorrowingResponse>> findMyActiveBorrowings(
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                borrowingService.findMyActiveBorrowings(borrowerId)
        );
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<BorrowingResponse>> findActiveBorrowersByBook(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        CurrentUser.requireAdmin(authentication);

        return ResponseEntity.ok(
                borrowingService.findActiveBorrowersByBook(bookId)
        );
    }

    @GetMapping("/books/{bookId}/history")
    public ResponseEntity<List<BorrowingResponse>> findBorrowingHistoryByBook(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        CurrentUser.requireAdmin(authentication);

        return ResponseEntity.ok(
                borrowingService.findBorrowingHistoryByBook(bookId)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<BorrowingResponse>> findAllActiveBorrowings(
            @RequestParam(defaultValue = "desc") String sort,
            Authentication authentication
    ) {

        CurrentUser.requireAdmin(authentication);

        return ResponseEntity.ok(
                borrowingService.findAllActiveBorrowings(sort)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<BorrowingResponse>> findAllBorrowingHistory(
            Authentication authentication
    ) {

        CurrentUser.requireAdmin(authentication);

        return ResponseEntity.ok(
                borrowingService.findAllBorrowingHistory()
        );
    }
}
