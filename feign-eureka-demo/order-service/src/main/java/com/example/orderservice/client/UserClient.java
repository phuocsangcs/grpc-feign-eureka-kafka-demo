package com.example.orderservice.client;

import com.example.orderservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Đây là "stub" của thế giới REST: bạn chỉ viết interface, khai method
 * y hệt 1 REST endpoint (dùng đúng annotation @GetMapping/@PathVariable
 * như bên Controller). Spring Cloud OpenFeign đọc annotation này, tự
 * TẠO RA 1 dynamic proxy lúc runtime implement interface này.
 *
 * name = "user-service" phải khớp với `spring.application.name` bên
 * user-service - Feign sẽ tra Eureka để tìm host:port thật ứng với tên
 * này (thay vì bạn tự khai địa chỉ như "grpc.client.user-service.address"
 * bên gRPC demo).
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable("id") Integer id);
}
