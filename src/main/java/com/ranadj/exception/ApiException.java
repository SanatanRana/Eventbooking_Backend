package com.ranadj.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Custom runtime exception to handle structured HTTP API failures.
 */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    /**
     * Constructs a new API exception with specified message and HTTP status code.
     *
     * @param message error description
     * @param status HTTP response status code
     */
    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
