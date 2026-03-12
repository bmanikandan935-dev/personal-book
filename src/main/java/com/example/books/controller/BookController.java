package com.example.books.controller;

import com.example.books.dto.GoogleBookSearchResponse;
import com.example.books.entity.Book;
import com.example.books.service.BookService;
import com.example.books.service.GoogleBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final GoogleBookService googleBookService;

    public BookController(BookService bookService, GoogleBookService googleBookService) {
        this.bookService = bookService;
        this.googleBookService = googleBookService;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        log.info("GET /books");
        return bookService.getAllBooks();
    }

    @PostMapping("/books/{googleId}")
    public ResponseEntity<Book> createBook(@PathVariable String googleId) {
        log.info("POST /books/{}", googleId);
        Book saved = bookService.createBookFromGoogleId(googleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/google")
    public GoogleBookSearchResponse searchGoogleBooks(@RequestParam("q") String query,
                                                      @RequestParam(value = "maxResults", required = false) Integer maxResults,
                                                      @RequestParam(value = "startIndex", required = false) Integer startIndex) {
        log.info("GET /google?q={}", query);
        return googleBookService.searchBooks(query, maxResults, startIndex);
    }
}
