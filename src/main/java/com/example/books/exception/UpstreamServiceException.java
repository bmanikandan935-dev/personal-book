package com.example.books.exception;

import org.springframework.http.HttpStatusCode;

public class UpstreamServiceException extends RuntimeException {
    private final HttpStatusCode status;

    public UpstreamServiceException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public UpstreamServiceException(HttpStatusCode status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
