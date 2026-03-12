package com.example.books.service;

import com.example.books.dto.GoogleBookResponse;
import com.example.books.entity.Book;
import com.example.books.exception.ValidationException;
import com.example.books.mapper.BookMapper;
import com.example.books.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private GoogleBookService googleBookService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void createBookFromGoogleId_success() {
        GoogleBookResponse.VolumeInfo volumeInfo = new GoogleBookResponse.VolumeInfo(
                "Effective Java",
                List.of("Joshua Bloch"),
                416
        );
        GoogleBookResponse response = new GoogleBookResponse("lRtdEAAAQBAJ", volumeInfo);
        Book mapped = Book.builder()
                .googleId("lRtdEAAAQBAJ")
                .title("Effective Java")
                .author("Joshua Bloch")
                .pageCount(416)
                .build();
        Book saved = Book.builder()
                .id(1L)
                .googleId("lRtdEAAAQBAJ")
                .title("Effective Java")
                .author("Joshua Bloch")
                .pageCount(416)
                .build();

        when(googleBookService.fetchVolumeById("lRtdEAAAQBAJ")).thenReturn(response);
        when(bookMapper.toBook(response)).thenReturn(mapped);
        when(bookRepository.save(mapped)).thenReturn(saved);

        Book result = bookService.createBookFromGoogleId("lRtdEAAAQBAJ");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Effective Java");
    }

    @Test
    void createBookFromGoogleId_validationFailure() {
        GoogleBookResponse.VolumeInfo volumeInfo = new GoogleBookResponse.VolumeInfo(
                null,
                List.of("Joshua Bloch"),
                416
        );
        GoogleBookResponse response = new GoogleBookResponse("lRtdEAAAQBAJ", volumeInfo);
        when(googleBookService.fetchVolumeById("lRtdEAAAQBAJ")).thenReturn(response);

        assertThatThrownBy(() -> bookService.createBookFromGoogleId("lRtdEAAAQBAJ"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("title");
    }

    @Test
    void createBookFromGoogleId_blankId_validationFailure() {
        assertThatThrownBy(() -> bookService.createBookFromGoogleId(" "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("id is required");
    }
}
