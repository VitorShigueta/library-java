package com.example.library.controllers;

import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.AuthorResponseDTO;
import com.example.library.dtos.PublisherRequestDTO;
import com.example.library.dtos.PublisherResponseDTO;
import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.services.PublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PublisherController Integration Tests")
public class PublisherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PublisherService publisherService;

    private PublisherRequestDTO defaultRequestDTO;
    private PublisherResponseDTO defaultResponseDTO;

    @BeforeEach
    void setUp() {
        defaultRequestDTO = new PublisherRequestDTO("Publisher Name", "www.publisher.com");
        defaultResponseDTO = new PublisherResponseDTO(1L, "Publisher Name", "www.publisher.com");
    }

    @Nested
    @DisplayName("POST /api/publishers")
    class CreatePublisherTests {

        @Test
        @WithMockUser
        @DisplayName("Should create an publisher and return 201 Created")
        void shouldCreateAuthorSuccessfully() throws Exception {
            when(publisherService.createPublisher(any(PublisherRequestDTO.class))).thenReturn(defaultResponseDTO);

            mockMvc.perform(post("/api/publishers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Publisher Name"))
                    .andExpect(jsonPath("$.website").value("www.publisher.com"));

            verify(publisherService, times(1)).createPublisher(any(PublisherRequestDTO.class));
        }

        @Test
        @DisplayName("Should return 403 Forbidden when user is not authenticated")
        void shouldReturnForbiddenWhenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/publishers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRequestDTO)))
                    .andExpect(status().isForbidden());

            verify(publisherService, never()).createPublisher(any());
        }
    }

    @Nested
    @DisplayName("GET /api/publishers/{id}")
    class FindByIdTests {

        @Test
        @WithMockUser
        @DisplayName("Should return publisher data and 200 OK when ID exists")
        void shouldReturnAuthorSuccessfully() throws Exception {
            when(publisherService.findById(anyLong())).thenReturn(defaultResponseDTO);

            mockMvc.perform(get("/api/publishers/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Publisher Name"))
                    .andExpect(jsonPath("$.website").value("www.publisher.com"));

            verify(publisherService, times(1)).findById(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 Not Found when ID does not exist")
        void shouldReturn404NotFoundWhenIDDoesNotExist() throws Exception {
            when(publisherService.findById(999L)).thenThrow(new ResourceNotFoundException("Publisher", 999L));

            mockMvc.perform(get("/api/publishers/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(publisherService, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("PUT /api/publishers/{id}")
    class UpdateAuthorTests {

        @Test
        @WithMockUser
        @DisplayName("Should update publisher and return 200 OK when publisher exists")
        void shouldUpdatePublisherSuccessfully() throws Exception {
            PublisherRequestDTO updateDTO = new PublisherRequestDTO("Publisher Name", "www.publisher.com");
            PublisherResponseDTO updatedResponse = new PublisherResponseDTO(1L, "New Publisher Name", "www.publisher.com");

            when(publisherService.updatePublisher(eq(1L), any(PublisherRequestDTO.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/publishers/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("New Publisher Name"))
                    .andExpect(jsonPath("$.website").value("www.publisher.com"));

            verify(publisherService, times(1)).updatePublisher(eq(1L), any(PublisherRequestDTO.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 Not Found when updating non-existent publisher")
        void shouldReturn404NotFoundWhenUpdatingNonExistentPublisher() throws Exception {
            PublisherRequestDTO updateDTO = new PublisherRequestDTO("Publisher Name", "www.publisher.com");

            when(publisherService.updatePublisher(eq(900L), any(PublisherRequestDTO.class))).thenThrow(new ResourceNotFoundException("Publisher", 900L));

            mockMvc.perform(put("/api/publishers/{id}", 900L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());

            verify(publisherService, times(1)).updatePublisher(eq(900L), any(PublisherRequestDTO.class));
        }
    }
}
