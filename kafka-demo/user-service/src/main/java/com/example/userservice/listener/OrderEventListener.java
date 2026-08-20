package com.example.userservice.listener;

import com.example.userservice.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Đây là "server" của thế giới Kafka - nhưng khác gRPC/Feign ở chỗ:
 * KHÔNG có ai chủ động "gọi" method này. Kafka tự invoke nó mỗi khi có
 * message mới trên topic "order-events" mà consumer group này chưa
 * đọc tới. order-service hoàn toàn không biết class này tồn tại.
 */
@Component
public class OrderEventListener {

    private final Map<Integer, String[]> fakeUserDb = new HashMap<>();

    public OrderEventListener() {
        fakeUserDb.put(1, new String[]{"Nguyen Van A", "a.nguyen@example.com"});
        fakeUserDb.put(2, new String[]{"Tran Thi B", "b.tran@example.com"});
    }

    @KafkaListener(topics = "order-events", groupId = "user-service-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("[UserService] Nhận được OrderCreatedEvent: " + event);

        String[] user = fakeUserDb.get(event.userId());
        if (user == null) {
            System.out.println("[UserService] Cảnh báo: order #" + event.orderId()
                    + " tham chiếu user_id=" + event.userId() + " không tồn tại trong DB");
            return;
        }

        // Mô phỏng việc xử lý bất đồng bộ: gửi email xác nhận, ghi lịch
        // sử mua hàng, tính điểm thưởng... - những việc KHÔNG cần chặn
        // order-service phải chờ mới trả response về cho client.
        System.out.println("[UserService] Gửi email xác nhận order #" + event.orderId()
                + " tới " + user[1] + " (" + user[0] + ")");
    }
}
