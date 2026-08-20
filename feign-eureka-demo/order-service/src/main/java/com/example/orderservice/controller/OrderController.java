package com.example.orderservice.controller;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.CreateOrderResponse;
import com.example.orderservice.dto.UserResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Microservice #2 - vừa expose REST API (POST /orders) vừa tự gọi sang
 * user-service qua UserClient (Feign) trước khi tạo order.
 */
@RestController
public class OrderController {

    // Feign tự inject implementation của interface này vào đây - bạn
    // không bao giờ tự viết class implements UserClient.
    private final UserClient userClient;

    private final AtomicInteger orderIdCounter = new AtomicInteger(1000);

    public OrderController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        System.out.println("[OrderService] Nhận request CreateOrder(userId="
                + request.userId() + ", product=" + request.product() + ")");

        UserResponse user;
        try {
            // ---- Đây là lời gọi REST sang user-service, thông qua Feign ----
            user = userClient.getUser(request.userId());
            // ------------------------------------------------------------------
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CreateOrderResponse(null, "FAILED", null));
        } catch (FeignException e) {
            // user-service không phản hồi / lỗi mạng / service down
            System.err.println("[OrderService] Gọi UserService thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new CreateOrderResponse(null, "FAILED", null));
        }

        if (!user.found()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CreateOrderResponse(null, "FAILED", null));
        }

        int orderId = orderIdCounter.getAndIncrement();
        System.out.println("[OrderService] Tạo order #" + orderId + " cho user '" + user.name() + "'");

        return ResponseEntity.ok(new CreateOrderResponse(orderId, "SUCCESS", user.name()));
    }
}
