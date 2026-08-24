package com.bms.library.exception;

public class BorrowingNotFoundException
        extends RuntimeException {

    public BorrowingNotFoundException(
            Long bookId,
            Long borrowerId
    ) {
        super(
                "No active borrowing found for book " +
                        bookId +
                        " and borrower " +
                        borrowerId
        );
    }
}
