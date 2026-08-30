package com.bms.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            AuthorNotFoundException.class,
            CategoryNotFoundException.class,
            BookNotFoundException.class,
            BookUnavailableException.class,
            ActiveBorrowingExistsException.class,
            BorrowingNotFoundException.class,
            UserServiceUnavailableException.class,
            UserProfileNotFoundException.class
    })
    public ProblemDetail handleBusinessException(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex) {
            case AuthorNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case CategoryNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case BookNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case BorrowingNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case BookUnavailableException ignored -> HttpStatus.CONFLICT;
            case ActiveBorrowingExistsException ignored -> HttpStatus.CONFLICT;
            case UserProfileNotFoundException ignored -> HttpStatus.BAD_GATEWAY;
            case UserServiceUnavailableException ignored -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detail.setTitle("Library request failed");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setTitle("Validation failed");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Validation failed");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }
}
