package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @EnableFeignClients bật cơ chế quét toàn bộ interface có @FeignClient
 * trong package này, rồi Spring tự tạo ra 1 implementation (dynamic proxy)
 * cho từng interface đó và đăng ký làm bean - y hệt tinh thần "stub" bên
 * gRPC, nhưng ở đây Spring tự sinh ra thay vì protoc sinh sẵn lúc build.
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
