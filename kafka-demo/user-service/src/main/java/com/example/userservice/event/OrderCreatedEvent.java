package com.example.userservice.event;

import java.time.Instant;

/**
 * Copy tay của event bên order-service - field name/type phải khớp để
 * Jackson deserialize đúng. Trong dự án thật nên tách ra 1 shared
 * library/artifact dùng chung (giống khuyến nghị với .proto ở demo gRPC).
 */
public record OrderCreatedEvent(
        Integer orderId,
        Integer userId,
        String product,
        Instant createdAt
) {
}
