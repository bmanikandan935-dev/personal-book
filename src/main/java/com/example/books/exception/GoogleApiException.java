package com.example.books.exception;

import org.springframework.http.HttpStatusCode;

public class GoogleApiException extends RuntimeException {
    private final HttpStatusCode status;

    public GoogleApiException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public GoogleApiException(HttpStatusCode status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
