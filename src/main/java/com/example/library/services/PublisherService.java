package com.example.library.services;

import com.example.library.dtos.PublisherRequestDTO;
import com.example.library.dtos.PublisherResponseDTO;
import com.example.library.entities.Publisher;
import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.repositories.PublisherRepository;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    private PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    public PublisherResponseDTO createPublisher(PublisherRequestDTO dto) {
        Publisher publisher = new Publisher();
        publisher.setName(dto.name());
        publisher.setWebsite(dto.website());

        Publisher savedPublisher = publisherRepository.save(publisher);

        return toResponseDTO(savedPublisher);
    }

    public PublisherResponseDTO findById(long id) {
        Publisher publisher = publisherRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Publisher", id));

        return toResponseDTO(publisher);
    }

    public PublisherResponseDTO updatePublisher(Long id, PublisherRequestDTO dto) {
        Publisher publisher = publisherRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Publisher", id));

        publisher.setName(dto.name());
        publisher.setWebsite(dto.website());

        Publisher updatedPublisher = publisherRepository.save(publisher);

        return toResponseDTO(updatedPublisher);
    }

    private PublisherResponseDTO toResponseDTO(Publisher publisher) {
        return new PublisherResponseDTO(
                publisher.getId(),
                publisher.getName(),
                publisher.getWebsite()
        );
    }
}
