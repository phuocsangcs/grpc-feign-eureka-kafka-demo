package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Đây là "danh bạ điện thoại" của toàn hệ thống - mọi service khi khởi
 * động sẽ tự đăng ký vào đây (kèm host:port của mình), và tra cứu ở đây
 * để tìm địa chỉ của service khác, thay vì hardcode "localhost:8081"
 * như cách làm tĩnh ở bên gRPC demo trước.
 *
 * Chạy tại http://localhost:8761 - có sẵn dashboard xem service nào đang sống.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
