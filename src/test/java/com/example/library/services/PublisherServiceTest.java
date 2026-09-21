package com.example.library.services;

import com.example.library.dtos.PublisherRequestDTO;
import com.example.library.dtos.PublisherResponseDTO;
import com.example.library.entities.Author;
import com.example.library.entities.Publisher;
import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.repositories.AuthorRepository;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Publisher service unit tests")
public class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherService publisherService;

    @Captor
    private ArgumentCaptor<Publisher> publisherCaptor;

    private PublisherRequestDTO defaultRequestDTO;
    private Publisher defaultSavedPublisher;

    @BeforeEach
    void setUp() {
        defaultRequestDTO = new PublisherRequestDTO("Publisher", "www.publisher.com");
        defaultSavedPublisher = createPublisher(1L, "Publisher", "www.publisher.com");
    }

    private Publisher createPublisher(Long id, String name, String website) {
        return new Publisher(id, name, website, new ArrayList<>());
    }

    @Nested
    @DisplayName("createPublisher()")
    class CreatePublisherTests {

        @Test
        @DisplayName("Should map DTO correctly and persist the publisher")
        void shouldCreateAndSavePublisherSuccessfully() {
            when(publisherRepository.save(any(Publisher.class))).thenReturn(defaultSavedPublisher);

            PublisherResponseDTO response = publisherService.createPublisher(defaultRequestDTO);

            verify(publisherRepository, times(1)).save(publisherCaptor.capture());
            Publisher capturedPublisher = publisherCaptor.getValue();

            assertNull(capturedPublisher.getId(), "The publisher ID should be null");
            assertEquals(defaultRequestDTO.name(), capturedPublisher.getName());
            assertEquals(defaultRequestDTO.website(), capturedPublisher.getWebsite());

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(defaultRequestDTO.name(), response.name());
            assertEquals(defaultRequestDTO.website(), response.website());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Should return publisher response DTO when id exists")
        void shouldFindPublisherByIdSuccessfully() {
            when(publisherRepository.findById(1L)).thenReturn(Optional.of(defaultSavedPublisher));

            PublisherResponseDTO response = publisherService.findById(1L);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(defaultRequestDTO.name(), response.name());

            verify(publisherRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResoureNotFoundException when publisher is not found")
        void shouldFindPublisherByIdNotFound() {
            when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> publisherService.findById(999L));
            verify(publisherRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("updatePublisher()")
    class UpdatePublisherTests {

        @Test
        @DisplayName("Should update publisher sucessfully when ID exists")
        void shouldUpdatePublisherSuccessfully() {
            Long publisherId = 1L;
            Publisher existingPublisher = createPublisher(publisherId, "Publisher", "www.publisher.com");
            PublisherRequestDTO updateDTO = new PublisherRequestDTO("New Publisher Name", "www.newpublisher.com");
            Publisher updatedPublisher = createPublisher(publisherId, updateDTO.name(), updateDTO.website());

            when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(existingPublisher));
            when(publisherRepository.save(existingPublisher)).thenReturn(updatedPublisher);

            PublisherResponseDTO response = publisherService.updatePublisher(publisherId, updateDTO);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("New Publisher Name", response.name());
            assertEquals("www.newpublisher.com", response.website());

            verify(publisherRepository, times(1)).findById(publisherId);
            verify(publisherRepository, times(1)).save(publisherCaptor.capture());

            Publisher capturedPublisher = publisherCaptor.getValue();
            assertEquals(publisherId, capturedPublisher.getId());
            assertEquals("New Publisher Name", capturedPublisher.getName());
            assertEquals("www.newpublisher.com", capturedPublisher.getWebsite());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when publisher is not found on update")
        void shouldUpdatePublisherNotFound() {
            Long nonExistentId = 999L;
            PublisherRequestDTO updateDTO = new PublisherRequestDTO("New Publisher Name", "www.newpublisher.com");

            when(publisherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> publisherService.updatePublisher(nonExistentId, updateDTO));

            verify(publisherRepository, times(1)).findById(nonExistentId);
            verify(publisherRepository, never()).save(any(Publisher.class));
        }
    }

}
