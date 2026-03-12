package com.example.books.service;

import com.example.books.config.GoogleBooksProperties;
import com.example.books.dto.GoogleBookResponse;
import com.example.books.dto.GoogleBookSearchResponse;
import com.example.books.exception.BookNotFoundException;
import com.example.books.exception.GoogleApiException;
import com.example.books.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatusCode;

@Service
public class GoogleBookService {

    private static final Logger log = LoggerFactory.getLogger(GoogleBookService.class);

    private final RestTemplate restTemplate;
    private final GoogleBooksProperties properties;

    public GoogleBookService(RestTemplate restTemplate, GoogleBooksProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public GoogleBookSearchResponse searchBooks(String query, Integer maxResults, Integer startIndex) {
        log.info("Google API search request q={}, maxResults={}, startIndex={}", query, maxResults, startIndex);
        try {
            return restTemplate.getForObject(
                    "/volumes?q={q}&maxResults={maxResults}&startIndex={startIndex}&key={key}",
                    GoogleBookSearchResponse.class,
                    query,
                    maxResults != null ? maxResults : 10,
                    startIndex != null ? startIndex : 0,
                    properties.getApiKey()
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new BookNotFoundException("No results found for query: " + query);
        } catch (HttpClientErrorException ex) {
            throw new GoogleApiException(ex.getStatusCode(), "Google API client error", ex);
        } catch (HttpServerErrorException ex) {
            throw mapServerError(ex);
        } catch (ResourceAccessException ex) {
            log.error("Google API search timeout q={}", query, ex);
            throw new UpstreamServiceException(HttpStatus.GATEWAY_TIMEOUT, "Google API timeout", ex);
        }
    }

    public GoogleBookResponse fetchVolumeById(String googleId) {
        log.info("Google API volume request googleId={}", googleId);
        try {
            return restTemplate.getForObject(
                    "/volumes/{id}?key={key}",
                    GoogleBookResponse.class,
                    googleId,
                    properties.getApiKey()
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new BookNotFoundException("Google volume not found for id: " + googleId);
        } catch (HttpClientErrorException ex) {
            throw new GoogleApiException(ex.getStatusCode(), "Google API client error", ex);
        } catch (HttpServerErrorException ex) {
            throw mapServerError(ex);
        } catch (ResourceAccessException ex) {
            log.error("Google API volume timeout googleId={}", googleId, ex);
            throw new UpstreamServiceException(HttpStatus.GATEWAY_TIMEOUT, "Google API timeout", ex);
        }
    }

    private RuntimeException mapServerError(HttpServerErrorException ex) {
        HttpStatusCode status = ex.getStatusCode();
        if (status.value() == HttpStatus.BAD_GATEWAY.value()
                || status.value() == HttpStatus.SERVICE_UNAVAILABLE.value()
                || status.value() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return new UpstreamServiceException(status, "Google API upstream error", ex);
        }
        return new GoogleApiException(status, "Google API server error", ex);
    }
}
