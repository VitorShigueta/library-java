package com.example.library.dtos;

import com.example.library.enums.UserRole;

import java.util.Set;

public record AssignRoleDTO(Set<UserRole> roles) {
}
