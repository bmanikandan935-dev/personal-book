package com.example.books.controller;

import com.example.books.entity.Book;
import com.example.books.exception.GlobalExceptionHandler;
import com.example.books.exception.UpstreamServiceException;
import com.example.books.exception.ValidationException;
import com.example.books.service.BookService;
import com.example.books.service.GoogleBookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private GoogleBookService googleBookService;

    @Test
    void postBooks_success_returns201() throws Exception {
        Book saved = Book.builder()
                .id(1L)
                .googleId("lRtdEAAAQBAJ")
                .title("Effective Java")
                .author("Joshua Bloch")
                .pageCount(416)
                .build();
        when(bookService.createBookFromGoogleId("lRtdEAAAQBAJ")).thenReturn(saved);

        mockMvc.perform(post("/books/lRtdEAAAQBAJ"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.googleId").value("lRtdEAAAQBAJ"))
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    void postBooks_invalid_returns400() throws Exception {
        when(bookService.createBookFromGoogleId(anyString()))
                .thenThrow(new ValidationException("Book title is missing"));

        mockMvc.perform(post("/books/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book title is missing"));
    }

    @Test
    void postBooks_upstreamError_returns503() throws Exception {
        when(bookService.createBookFromGoogleId(anyString()))
                .thenThrow(new UpstreamServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Google API upstream error"));

        mockMvc.perform(post("/books/any"))
                .andExpect(status().isServiceUnavailable());
    }
}
