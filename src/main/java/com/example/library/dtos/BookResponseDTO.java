package com.example.library.dtos;

import java.math.BigDecimal;
import java.util.Set;

public record BookResponseDTO(Long id, String title, String isbn, Integer publicationYear, BigDecimal price, Long publisherId, Set<Long> authorIds) {}
