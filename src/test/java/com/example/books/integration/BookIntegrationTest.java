package com.example.books.integration;

import com.example.books.entity.Book;
import com.example.books.repository.BookRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Environment environment;

    @Test
    void postBooks_persistsBook() {
        String apiKey = environment.getProperty("google.books.api.key");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "Google API key required for integration test");

        bookRepository.deleteAll();
        String googleId = "lRtdEAAAQBAJ";
        ResponseEntity<Book> response = restTemplate.postForEntity("/books/" + googleId, null, Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Book body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getGoogleId()).isEqualTo(googleId);

        assertThat(bookRepository.findAll()).extracting(Book::getGoogleId).contains(googleId);
    }
}
