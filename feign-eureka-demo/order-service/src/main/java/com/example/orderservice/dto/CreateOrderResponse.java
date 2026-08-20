package com.example.orderservice.dto;

public record CreateOrderResponse(
        Integer orderId,
        String status,
        String userName
) {
}
