package com.example.library.dtos;

import com.example.library.repositories.UserRepository;

public record AuthenticationDTO(String email, String password) {
}
