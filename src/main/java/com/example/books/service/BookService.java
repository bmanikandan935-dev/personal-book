package com.example.books.service;

import com.example.books.dto.GoogleBookResponse;
import com.example.books.entity.Book;
import com.example.books.exception.ValidationException;
import com.example.books.mapper.BookMapper;
import com.example.books.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final GoogleBookService googleBookService;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, GoogleBookService googleBookService, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.googleBookService = googleBookService;
        this.bookMapper = bookMapper;
    }

    public List<Book> getAllBooks() {
        log.info("Fetching all books");
        return bookRepository.findAll();
    }

    public Book createBookFromGoogleId(String googleId) {
        log.info("Create book from Google ID request: {}", googleId);
        validateGoogleId(googleId);
        GoogleBookResponse response = googleBookService.fetchVolumeById(googleId);
        validateResponse(response);
        Book book = bookMapper.toBook(response);
        if (book == null) {
            log.warn("Validation failed: mapper returned null for googleId={}", googleId);
            throw new ValidationException("Mapped book payload is empty");
        }
        log.info("Saving book googleId={}, title={}", book.getGoogleId(), book.getTitle());
        return bookRepository.save(book);
    }

    private void validateGoogleId(String googleId) {
        if (googleId == null || googleId.isBlank()) {
            log.warn("Validation failed: googleId is blank");
            throw new ValidationException("Google book id is required");
        }
    }

    private void validateResponse(GoogleBookResponse response) {
        if (response == null) {
            log.warn("Validation failed: response is null");
            throw new ValidationException("Google response is empty");
        }
        if (response.volumeInfo() == null) {
            log.warn("Validation failed: volumeInfo is null");
            throw new ValidationException("Google response volumeInfo is empty");
        }
        if (response.volumeInfo().title() == null || response.volumeInfo().title().isBlank()) {
            log.warn("Validation failed: title is empty");
            throw new ValidationException("Book title is missing");
        }
        if (response.volumeInfo().authors() == null || response.volumeInfo().authors().isEmpty()) {
            log.warn("Validation failed: authors empty");
            throw new ValidationException("Book authors are missing");
        }
    }
}
