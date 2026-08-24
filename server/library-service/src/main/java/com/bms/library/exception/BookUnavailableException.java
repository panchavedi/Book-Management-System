package com.bms.library.exception;

public class BookUnavailableException
        extends RuntimeException {

    public BookUnavailableException(Long id) {
        super(
                "Book with id " + id + " has no available copies"
        );
    }
}