package com.example.library.services;

import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.AuthorResponseDTO;
import com.example.library.entities.Author;
import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.repositories.AuthorRepository;
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

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorService unit tests")
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Captor
    private ArgumentCaptor<Author> authorCaptor;

    private AuthorRequestDTO defaultRequestDTO;
    private Author defaultSavedAuthor;

    @BeforeEach
    void setUp() {
        defaultRequestDTO = new AuthorRequestDTO("Machado de Assis", "Escritor e fundador da ABL.");
        defaultSavedAuthor = createAuthor(1L, "Machado de Assis", "Escritor e fundador da ABL.");
    }

    private Author createAuthor(Long id, String name, String biography) {
        return new Author(id, name, biography, new HashSet<>());
    }

    @Nested
    @DisplayName("createAuthor()")
    class createAuthorTests {

        @Test
        @DisplayName("Should map DTO correctly and persist the author.")
        void shouldCreateAndSaveAuthorSuccessfully() {
            when(authorRepository.save(any(Author.class))).thenReturn(defaultSavedAuthor);

            AuthorResponseDTO response = authorService.createAuthor(defaultRequestDTO);

            verify(authorRepository, times(1)).save(authorCaptor.capture());
            Author capturedAuthor = authorCaptor.getValue();

            assertNull(capturedAuthor.getId(), "The author ID should be null");
            assertEquals(defaultRequestDTO.name(), capturedAuthor.getName());
            assertEquals(defaultRequestDTO.biography(), capturedAuthor.getBiography());

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(defaultRequestDTO.name(), response.name());
            assertEquals(defaultRequestDTO.biography(), response.biography());
        }
    }

    @Nested
    @DisplayName("findById()")
    class findByIdTests {

        @Test
        @DisplayName("Should return author response DTO when id exists")
        void shouldReturnTheAuthorDTOWhenIdExists() {
            when(authorRepository.findById(1L)).thenReturn(Optional.of(defaultSavedAuthor));

            AuthorResponseDTO response = authorService.findById(1L);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(defaultRequestDTO.name(), response.name());

            verify(authorRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when author is not found")
        void shouldThrowResourceNotFoundExceptionWhenIdIsNotFound() {
            when(authorRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authorService.findById(999L));
            verify(authorRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("updateAuthor")
    class updateAuthorTests {

        @Test
        @DisplayName("Should update author sucessfuly when id exists")
        void shouldUpdateAuthorSuccessfully() {
            Long authorId = 1L;
            Author existingAuthor = new Author(authorId, "Takibi Amamori","Exemple bio",new HashSet<>());
            AuthorRequestDTO updateDTO = new AuthorRequestDTO("Takibi amamori", "new bio");

            Author updatedAuthor = new Author(authorId,  updateDTO.name(), updateDTO.biography(), new HashSet<>());

            when(authorRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
            when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

            AuthorResponseDTO response = authorService.updateAuthor(authorId, updateDTO);

            assertNotNull(response);
            assertEquals(authorId, response.id());
            assertEquals(updateDTO.name(), response.name());
            assertEquals(updateDTO.biography(), response.biography());

            verify(authorRepository, times(1)).findById(authorId);
            verify(authorRepository, times(1)).save(authorCaptor.capture());

            Author capturedAuthor = authorCaptor.getValue();
            assertEquals(authorId, capturedAuthor.getId());
            assertEquals("Takibi amamori", capturedAuthor.getName());
            assertEquals("new bio", capturedAuthor.getBiography());
        }

        @Test
        @DisplayName("Should throw exception exception and not call save when author is not found")
        void shouldThrowExceptionWhenAuthorIsNotFound() {
            Long nonExistentAuthorId = 1L;
            AuthorRequestDTO updateDTO = new AuthorRequestDTO("Takibi amori", "bio");

            when(authorRepository.findById(nonExistentAuthorId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authorService.updateAuthor(nonExistentAuthorId, updateDTO));

            verify(authorRepository, times(1)).findById(nonExistentAuthorId);
            verify(authorRepository, never()).save(any(Author.class));
        }
    }
}
