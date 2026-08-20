package com.example.orderservice.event;

import java.time.Instant;

/**
 * "Hợp đồng" của message gửi qua Kafka - tương đương message trong .proto
 * bên gRPC, nhưng ở đây KHÔNG có công cụ nào ép buộc 2 bên phải khớp
 * field với nhau (Kafka không biết gì về cấu trúc payload). Nếu
 * user-service và order-service định nghĩa lệch nhau, lỗi chỉ lộ ra lúc
 * runtime khi deserialize - đây là đánh đổi so với contract cứng của
 * protobuf.
 */
public record OrderCreatedEvent(
        Integer orderId,
        Integer userId,
        String product,
        Instant createdAt
) {
}
