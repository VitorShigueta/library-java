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

    @Nested
    @DisplayName("findBooksById()")
    class FindBooksByIdTests {

        @Test
        @DisplayName("Should return book response DTO when ID existis")
        void shouldFindBookIDSuccessfully() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(defaultSavedBook));

            BookResponseDTO response = bookService.findBookById(1L);

            assertNotNull(response);
            assertAll("Validade all BookResponseDTO fields",
                    () -> assertEquals(1L, response.id()),
                    () -> assertEquals(defaultSavedBook.getTitle(), response.title()),
                    () -> assertEquals(defaultSavedBook.getIsbn(), response.isbn()),
                    () -> assertEquals(defaultSavedBook.getPublicationYear(), response.publicationYear()),
                    () -> assertEquals(defaultSavedBook.getPrice(), response.price()),
                    () -> assertEquals(1L, response.publisherId()),
                    () -> assertEquals(Set.of(1L), response.authorIds())
            );

            verify(bookRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when book is not found")
        void shouldThrowExceptionWhenBookIsNotFound() {
            when(bookRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.findBookById(999L));

            verify(bookRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("updateBookById()")
    class updateBookByidTest {

        @Test
        @DisplayName("Should update book sucessfully when book, publisher and auhtor exist")
        void shouldUpdateBookSuccessfully() {
            Long bookId = 1L;
            Book existingBook = createBook(bookId, "Old Tile", "11111111111110", 2010, new BigDecimal("20.00"), defaultPublisher, Set.of(defaultAuthor));

            BookRequestDTO updateDTO = new BookRequestDTO("Updated Title","9784048926669", 2020, new BigDecimal("45.00"), 1L, Set.of(1L));
            Book updatedSavedBook = createBook(bookId, updateDTO.title(), updateDTO.isbn(), updateDTO.publicationYear(), updateDTO.price(), defaultPublisher, Set.of(defaultAuthor));

            when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
            when(publisherRepository.findById(updateDTO.publisherId())).thenReturn(Optional.of(defaultPublisher));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedSavedBook);
            when(authorRepository.findAllById(updateDTO.authorIds())).thenReturn(List.of(defaultAuthor));

            BookResponseDTO responseDTO = bookService.updateBookById(bookId, updateDTO);

            verify(bookRepository, times(1)).findById(bookId);
            verify(publisherRepository, times(1)).findById(2L);
            verify(authorRepository, times(1)).findAllById(Set.of(2L));
            verify(bookRepository, times(1)).save(bookArgumentCaptor.capture());

            Book captured = bookArgumentCaptor.getValue();

            assertAll("Validation of the Book entity captured in save()",
                    () -> assertEquals(bookId, captured.getId()),
                    () -> assertEquals(updateDTO.title(), captured.getTitle()),
                    () -> assertEquals(updateDTO.isbn(), captured.getIsbn()),
                    () -> assertEquals(updateDTO.price(), captured.getPrice()),
                    () -> assertEquals(updateDTO.publicationYear(), captured.getPublicationYear()),
                    () -> assertEquals(updateDTO.publisherId(), captured.getPublisher().getId()),
                    () -> assertTrue(captured.getAuthors().contains(defaultAuthor))
            );

            assertNotNull(responseDTO);
            assertAll("Validation of all fields in the returned BookResponseDTO",
                    () -> assertEquals());

        }
    }
}
