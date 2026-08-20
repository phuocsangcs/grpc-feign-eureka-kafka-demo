package com.example.orderservice.dto;

public record UserResponse(
        Integer userId,
        String name,
        String email,
        boolean found
) {
}
