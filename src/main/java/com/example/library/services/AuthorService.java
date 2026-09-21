package com.example.library.services;

import com.example.library.exceptions.ResourceNotFoundException;
import com.example.library.dtos.AuthorRequestDTO;
import com.example.library.dtos.AuthorResponseDTO;
import com.example.library.entities.Author;
import com.example.library.repositories.AuthorRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public AuthorResponseDTO createAuthor(AuthorRequestDTO authorRequestDTO) {
        Author author = new Author();
        author.setName(authorRequestDTO.name());
        author.setBiography(authorRequestDTO.biography());

        Author savedAuthor = authorRepository.save(author);

        return new AuthorResponseDTO(
                savedAuthor.getId(),
                savedAuthor.getName(),
                savedAuthor.getBiography()
        );
    }

    public AuthorResponseDTO findById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));

        return new AuthorResponseDTO(
                author.getId(),
                author.getName(),
                author.getBiography()
        );
    }


    public AuthorResponseDTO updateAuthor(Long authorId, AuthorRequestDTO updateDTO) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));

        author.setName(updateDTO.name());
        author.setBiography(updateDTO.biography());

        Author savedAuthor = authorRepository.save(author);

        return  new  AuthorResponseDTO(savedAuthor.getId(), savedAuthor.getName(), savedAuthor.getBiography());
    }
}
