package com.example.library.controllers;

import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.AuthorResponseDTO;
import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.repositories.AuthorRepository;
import com.example.library.services.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthorController Integration Tests")
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthorService authorService;

    private AuthorRequestDTO defaultRequestDTO;
    private AuthorResponseDTO defaultResponseDTO;

    @BeforeEach
    void setUp() {
        defaultRequestDTO = new AuthorRequestDTO("Hyuganatsu", "Bio");
        defaultResponseDTO = new AuthorResponseDTO(1L, "Hyuganatsu", "Bio");
    }

    @Nested
    @DisplayName("POST /api/authors")
    class CreateAuthorTests {

        @Test
        @WithMockUser
        @DisplayName("Should create an author and return 201 Created")
        void shouldCreateAuthorSuccessfully() throws Exception {
            when(authorService.createAuthor(any(AuthorRequestDTO.class))).thenReturn(defaultResponseDTO);

            mockMvc.perform(post("/api/authors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Hyuganatsu"))
                    .andExpect(jsonPath("$.biography").value("Bio"));

            verify(authorService, times(1)).createAuthor(any(AuthorRequestDTO.class));
        }

        @Test
        @DisplayName("Should return 403 Forbidden when user is not authenticated")
        void shouldReturnForbiddenWhenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(defaultRequestDTO)))
                    .andExpect(status().isForbidden());

            verify(authorService, never()).createAuthor(any());
        }
    }

    @Nested
    @DisplayName("GET /api/authors/{id}")
    class FindByIdTests {

        @Test
        @WithMockUser
        @DisplayName("Should return author data and 200 OK when ID exists")
        void shouldReturnAuthorSuccessfully() throws Exception {
            when(authorService.findById(anyLong())).thenReturn(defaultResponseDTO);

            mockMvc.perform(get("/api/authors/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Hyuganatsu"))
                    .andExpect(jsonPath("$.biography").value("Bio"));

            verify(authorService, times(1)).findById(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 Not Found when ID does not exist")
        void shouldReturn404NotFoundWhenIDDoesNotExist() throws Exception {
            when(authorService.findById(999L)).thenThrow(new ResourceNotFoundException("Author", 999L));

            mockMvc.perform(get("/api/authors/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(authorService, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("PUT /api/auhtors/{id}")
    class UpdateAuthorTests {

        @Test
        @WithMockUser
        @DisplayName("Should update author and return 200 OK when author exists")
        void shouldUpdateAuthorSuccessfully() throws Exception {
            AuthorRequestDTO updateDTO = new AuthorRequestDTO("Hyuganatsu", "Bio");
            AuthorResponseDTO updatedResponse = new AuthorResponseDTO(1L, "Hyuganatsu", "New Bio");

            when(authorService.updateAuthor(eq(1L), any(AuthorRequestDTO.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/authors/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Hyuganatsu"))
                    .andExpect(jsonPath("$.biography").value("New Bio"));

            verify(authorService, times(1)).updateAuthor(eq(1L), any(AuthorRequestDTO.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 Not Found when updating non-existent author")
        void shouldReturn404NotFoundWhenUpdatingNonExistentAuthor() throws Exception {
            AuthorRequestDTO updateDTO = new AuthorRequestDTO("Hyuganatsu", "Bio");

            when(authorService.updateAuthor(eq(900L), any(AuthorRequestDTO.class))).thenThrow(new ResourceNotFoundException("Author", 900L));

            mockMvc.perform(put("/api/authors/{id}", 900L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());

            verify(authorService, times(1)).updateAuthor(eq(900L), any(AuthorRequestDTO.class));
        }
    }
}
