package com.example.books.exception;

import com.example.books.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BookNotFoundException ex, HttpServletRequest request) {
        log.warn("Book not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(GoogleApiException.class)
    public ResponseEntity<ErrorResponse> handleGoogleApi(GoogleApiException ex, HttpServletRequest request) {
        log.error("Google API error: {}", ex.getMessage(), ex);
        return buildError(ex.getStatus(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ErrorResponse> handleUpstream(UpstreamServiceException ex, HttpServletRequest request) {
        log.error("Upstream service error: {}", ex.getMessage(), ex);
        return buildError(ex.getStatus(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClient(HttpClientErrorException ex, HttpServletRequest request) {
        log.error("HTTP client error: {}", ex.getMessage(), ex);
        return buildError(ex.getStatusCode(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServer(HttpServerErrorException ex, HttpServletRequest request) {
        log.error("HTTP server error: {}", ex.getMessage(), ex);
        return buildError(ex.getStatusCode(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatusCode status, String message, String path) {
        HttpStatus resolvedStatus = HttpStatus.resolve(status.value());
        String error = resolvedStatus != null ? resolvedStatus.getReasonPhrase() : "HTTP " + status.value();
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), error, message, path);
        return ResponseEntity.status(status).body(body);
    }
}
