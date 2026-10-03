package com.example.library.services;

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
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, PublisherRepository publisherRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
    }


    public BookResponseDTO createBook(BookRequestDTO dto) {
        Publisher publisher = publisherRepository.findById(dto.publisherId())
                .orElseThrow(() -> new ResourceNotFoundException("Publisher", dto.publisherId()));

        Set<Author> authors = findAndValidateAuthors(dto.authorIds());

        Book book = new Book();
    }

    private Set<Author> findAndValidateAuthors(Set<Long> authorsIds) {
        if (authorsIds == null || authorsIds.isEmpty()) {
            throw new ResourceNotFoundException("Author", null);
        }

        List<Author> foundAuthors = authorRepository.findAllById(authorsIds);

        if (foundAuthors.size() != authorsIds.size()) {
            Set<Long> foundIds = foundAuthors.stream()
                    .map(Author::getId)
                    .collect(Collectors.toSet());

            Long missingId = authorsIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElse(null);

            throw new ResourceNotFoundException("Author", missingId);
        }

        return new HashSet<>(foundAuthors);
    }

    private BookResponseDTO toResponseDTO(Book book) {
        Long publisherId = book.getPublisher() != null ? book.getPublisher().getId() : null;

        Set<Long> auhthorsId = book.getAuthors() != null
                ? book.getAuthors().stream().map(Author::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getPrice(),
                publisherId,
                auhthorsId
        );
    }
}
