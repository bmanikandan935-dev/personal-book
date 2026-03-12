package com.example.books.controller;

import com.example.books.entity.Book;
import com.example.books.repository.BookRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExistingEndpointsTest {

    static MockWebServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("google.books.base-url", () -> server.url("/").toString());
        registry.add("google.books.api.key", () -> "test-key");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void getBooks_returnsSavedBooks() throws Exception {
        bookRepository.deleteAll();
        bookRepository.save(Book.builder().googleId("one").title("Title One").author("Author A").pageCount(100).build());
        bookRepository.save(Book.builder().googleId("two").title("Title Two").author("Author B").pageCount(200).build());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title One"))
                .andExpect(jsonPath("$[1].title").value("Title Two"));
    }

    @Test
    void getGoogle_returnsRawSchema() throws Exception {
        Path path = Paths.get("src", "test", "resources", "effectivejava.json");
        String body = Files.readString(path);
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        mockMvc.perform(get("/google").param("q", "effective+java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("books#volumes"))
                .andExpect(jsonPath("$.items[0].volumeInfo.title").value("Effective Java"));
    }
}
