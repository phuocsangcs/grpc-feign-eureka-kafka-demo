package com.example.orderservice.dto;

public record CreateOrderRequest(
        Integer userId,
        String product
) {
}
