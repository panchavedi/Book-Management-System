package com.bms.library.service;

import com.bms.library.dto.BorrowingResponse;

import java.util.List;

public interface BorrowingService {

    List<BorrowingResponse> findMyBorrowingHistory(
            Long borrowerId
    );

    List<BorrowingResponse> findMyActiveBorrowings(
            Long borrowerId
    );

    List<BorrowingResponse> findActiveBorrowersByBook(
            Long bookId
    );

    List<BorrowingResponse> findAllActiveBorrowings(
            String sort
    );
}
