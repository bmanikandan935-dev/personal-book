package com.example.books.repository;

import com.example.books.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void saveAndFindAll() {
        Book book1 = Book.builder().googleId("one").title("Title One").author("Author A").pageCount(100).build();
        Book book2 = Book.builder().googleId("two").title("Title Two").author("Author B").pageCount(200).build();
        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Title One", "Title Two");
    }
}
