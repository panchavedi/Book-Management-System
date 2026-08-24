package com.bms.library.exception;

public class ActiveBorrowingExistsException
        extends RuntimeException {

    public ActiveBorrowingExistsException(
            Long bookId,
            Long borrowerId
    ) {
        super(
                "Borrower " + borrowerId +
                 " already has an active borrowing for book " + bookId
        );
    }
}
