package com.example.library.controllers;

import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.AuthorResponseDTO;
import com.example.library.dtos.PublisherRequestDTO;
import com.example.library.dtos.PublisherResponseDTO;
import com.example.library.services.PublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<PublisherResponseDTO> createPublisher(@RequestBody PublisherRequestDTO dto) {
        PublisherResponseDTO publisher = publisherService.createPublisher(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(publisher);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> findById(@PathVariable Long id) {
        PublisherResponseDTO publisher = publisherService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(publisher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> update(@PathVariable Long id, @RequestBody PublisherRequestDTO dto) {
        PublisherResponseDTO publisher  = publisherService.updatePublisher(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(publisher);
    }
}
