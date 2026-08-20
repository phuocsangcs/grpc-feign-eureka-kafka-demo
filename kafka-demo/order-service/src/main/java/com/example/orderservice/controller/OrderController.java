package com.example.orderservice.controller;

import com.example.orderservice.event.OrderCreatedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class OrderController {

    private static final String TOPIC = "order-events";

    // KafkaTemplate là "producer stub" - tương đương KafkaProducer thuần
    // nhưng được Spring auto-config sẵn từ application.yml, không cần
    // tự tạo Properties + KafkaProducer thủ công.
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    private final AtomicInteger orderIdCounter = new AtomicInteger(1000);

    public OrderController(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        Integer userId = (Integer) body.get("userId");
        String product = (String) body.get("product");
        int orderId = orderIdCounter.getAndIncrement();

        // Khác biệt LỚN NHẤT so với gRPC/Feign: order-service KHÔNG hỏi
        // user-service xem user có tồn tại hay không trước khi tạo order.
        // Nó tạo order ngay, coi như thành công, rồi PHÁT sự kiện ra -
        // ai quan tâm (user-service, notification-service, v.v...) tự xử lý.
        // Đây là đánh đổi: nhanh, decoupled, nhưng chấp nhận "eventual
        // consistency" - có thể vài trăm ms sau consumer mới xử lý xong.
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, product, Instant.now());

        // key = String.valueOf(userId): đảm bảo mọi event của CÙNG 1 user
        // luôn rơi vào cùng 1 partition -> giữ đúng thứ tự xử lý cho user đó
        kafkaTemplate.send(TOPIC, String.valueOf(userId), event);

        System.out.println("[OrderService] Đã publish OrderCreatedEvent: " + event);

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "status", "PUBLISHED"
        ));
    }
}
