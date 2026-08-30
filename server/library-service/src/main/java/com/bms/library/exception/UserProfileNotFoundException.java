package com.bms.library.exception;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(Long userId, Throwable cause) {
        super("User " + userId + " was not found in User Service", cause);
    }
}
