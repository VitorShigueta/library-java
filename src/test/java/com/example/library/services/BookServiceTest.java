package com.example.library.services;

import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.BookRequestDTO;
import com.example.library.entities.Author;
import com.example.library.entities.Book;
import com.example.library.entities.Publisher;
import com.example.library.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService unit tests")
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Captor
    private ArgumentCaptor<Book> bookArgumentCaptor;

    private BookRequestDTO defaultRequestDTO;
    private Book defaultSavedBook;

    @BeforeEach
    void setUp() {
        defaultRequestDTO = new BookRequestDTO();
        defaultSavedBook = createBook();
    }

    private Book createBook(Long id, String title, String isbn, Integer publicationYear, BigDecimal price) {
        return new Book(id, title, isbn, publicationYear, price, new Publisher(), new HashSet<>(), new ArrayList<>());
    }
}
