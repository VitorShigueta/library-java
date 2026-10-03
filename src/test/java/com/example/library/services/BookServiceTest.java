package com.example.library.services;

import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.AuthorResponseDTO;
import com.example.library.dtos.BookRequestDTO;
import com.example.library.dtos.BookResponseDTO;
import com.example.library.entities.Author;
import com.example.library.entities.Book;
import com.example.library.entities.Publisher;
import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.repositories.AuthorRepository;
import com.example.library.repositories.BookRepository;
import com.example.library.repositories.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.management.relation.RelationServiceNotRegisteredException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService unit tests")
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    @Captor
    private ArgumentCaptor<Book> bookArgumentCaptor;

    private BookRequestDTO defaultRequestDTO;
    private Book defaultSavedBook;
    private Publisher defaultPublisher;
    private Author defaultAuthor;

    @BeforeEach
    void setUp() {
        defaultPublisher = new Publisher(1L, "Kadokawa", "https://kadokawa.co.jp", new ArrayList<>());
        defaultAuthor = new Author(1L, "Asato Asato", "Light Novel author", new HashSet<>());
        defaultRequestDTO = new BookRequestDTO("86--EIGHTY-SIX, Vol. 1", "9784048926669", 2017, new BigDecimal("30.00"), 1L, Set.of(1L));
        defaultSavedBook = createBook(1L, "86--EIGHTY-SIX, Vol. 1", "9784048926669", 2017, new BigDecimal("30.00"), defaultPublisher, Set.of(defaultAuthor));
    }

    private Book createBook(Long id, String title, String isbn, Integer publicationYear, BigDecimal price, Publisher publisher, Set<Author> authors) {
        return new Book(id, title, isbn, publicationYear, price, publisher, new HashSet<>(authors), new ArrayList<>());
    }

    @Nested
    @DisplayName("createBook()")
    class createBookTests {
        @Test
        @DisplayName("Should map DTO correctly and persist the book.")
        void shouldCreateAndSaveBookSuccessfully() {
            when(publisherRepository.findById(1L)).thenReturn(Optional.of(defaultPublisher));
            when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(defaultAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(defaultSavedBook);

            BookResponseDTO response = bookService.createBook(defaultRequestDTO);

            verify(publisherRepository, times(1)).findById(1L);
            verify(authorRepository, times(1)).findAllById(Set.of(1L));

            verify(bookRepository, times(1)).save(bookArgumentCaptor.capture());
            Book capturedBook = bookArgumentCaptor.getValue();

            assertNull(capturedBook.getId(), "The Book ID should be null");
            assertEquals(defaultRequestDTO.title(), capturedBook.getTitle());
            assertEquals(defaultRequestDTO.isbn(), capturedBook.getIsbn());
            assertEquals(defaultRequestDTO.publicationYear(), capturedBook.getPublicationYear());
            assertEquals(defaultRequestDTO.price(), capturedBook.getPrice());
            assertEquals(defaultPublisher, capturedBook.getPublisher());
            assertTrue(capturedBook.getAuthors().contains(defaultAuthor));

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(defaultRequestDTO.title(), response.title());
            assertEquals(defaultRequestDTO.isbn(), response.isbn());
            assertEquals(defaultRequestDTO.publicationYear(), response.publicationYear());
            assertEquals(defaultRequestDTO.price(), response.price());
            assertEquals(1L, response.publisherId());
            assertEquals(Set.of(1L), response.authorIds());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException and not save when publishes does not exist")
        void shouldThrowExceptionWhenPublisherNotFound() {
            when(publisherRepository.findById(defaultRequestDTO.publisherId())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(defaultRequestDTO));

            verify(publisherRepository, times(1)).findById(defaultRequestDTO.publisherId());
            verify(authorRepository, never()).findAllById(any());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException and not save when author does not exist")
        void shouldThrowExceptionWhenAuthorNotFound() {
            when(publisherRepository.findById(defaultRequestDTO.publisherId())).thenReturn(Optional.of(defaultPublisher));
            when(authorRepository.findAllById(defaultRequestDTO.authorIds())).thenReturn(List.of());

            assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(defaultRequestDTO));

            verify(publisherRepository, times(1)).findById(defaultRequestDTO.publisherId());
            verify(authorRepository, times(1)).findAllById(defaultRequestDTO.authorIds());
            verify(bookRepository, never()).save(any(Book.class));
        }
    }
}
