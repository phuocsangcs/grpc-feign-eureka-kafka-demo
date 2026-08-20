# Demo: 2 microservice giao tiếp bất đồng bộ qua Apache Kafka

Khác biệt so với gRPC/Feign: order-service KHÔNG gọi trực tiếp
user-service. Nó chỉ PHÁT (publish) 1 sự kiện `OrderCreatedEvent` lên
Kafka, còn user-service tự LẮNG NGHE (subscribe) và xử lý khi rảnh -
2 service hoàn toàn không biết sự tồn tại của nhau, chỉ biết chung 1
"topic".

## Cấu trúc
```
kafka-demo/
├── docker-compose.yml    # chạy 1 Kafka broker (KRaft mode, không cần Zookeeper)
├── order-service/        # PRODUCER - publish event khi tạo order, port 8082
└── user-service/         # CONSUMER - lắng nghe event, port 8081
```

## Bước 1: chạy Kafka broker
```bash
docker compose up -d
# Kiểm tra broker sống: docker logs kafka
```

## Bước 2: chạy 2 service (2 terminal)
```bash
# Terminal 1
cd user-service
mvn spring-boot:run

# Terminal 2
cd order-service
mvn spring-boot:run
```

## Test thử
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "product": "Laptop Dell XPS"}'
```

`order-service` trả response **ngay lập tức** (`{"orderId":1000,"status":"PUBLISHED"}`)
mà không chờ user-service xử lý xong. Nhìn log của `user-service`, bạn
sẽ thấy nó tự in ra dòng "Gửi email xác nhận..." vài chục ms sau đó -
độc lập, không đồng bộ với response mà order-service đã trả về.

## Các khái niệm Kafka cốt lõi trong demo này

- **Topic** (`order-events`): "kênh" chứa message, giống tên 1 hàng đợi
  có thể có nhiều producer/consumer cùng dùng.
- **Producer** (`KafkaTemplate` trong order-service): chỉ có nhiệm vụ
  gửi message vào topic, không quan tâm ai đọc, đọc lúc nào.
- **Consumer** (`@KafkaListener` trong user-service): tự động được
  Kafka gọi mỗi khi có message mới chưa đọc.
- **Consumer group** (`group-id: user-service-group`): nếu bạn chạy 2
  instance user-service cùng group này, Kafka tự chia đôi lượng message
  cho 2 instance xử lý (scale ngang tự nhiên). Nếu 2 service khác nhau
  dùng 2 group-id khác nhau cùng subscribe 1 topic, CẢ HAI đều nhận
  được toàn bộ message (broadcast) - đây là cách nhiều service khác
  nhau (notification-service, inventory-service...) có thể cùng lắng
  nghe 1 sự kiện `OrderCreatedEvent` mà không cần order-service biết
  tới sự tồn tại của chúng.
- **Key** (`String.valueOf(userId)` khi gửi): quyết định message rơi
  vào partition nào - dùng để đảm bảo thứ tự xử lý cho cùng 1 entity
  (ở đây là cùng 1 user) dù topic có nhiều partition.
- **Offset**: Kafka lưu message trên đĩa (mặc định 7 ngày), consumer
  chỉ "di chuyển con trỏ" offset khi đọc xong - nếu user-service down
  rồi lên lại, nó tiếp tục đọc từ offset cũ, không mất message.

## So sánh 3 cách đã làm

| | gRPC | Feign + Eureka | Kafka |
|---|---|---|---|
| Mô hình | Sync RPC | Sync REST | Async event |
| order-service cần user-service sống? | Có (mới trả lời được request) | Có | Không |
| Ai biết ai | 2 bên biết nhau trực tiếp | 2 bên biết nhau qua Eureka | Chỉ biết chung topic, không biết nhau |
| Thêm 1 bên thứ 3 quan tâm event | Phải sửa code order-service để gọi thêm | Phải sửa code order-service để gọi thêm | Chỉ cần thêm 1 consumer mới subscribe cùng topic, KHÔNG sửa order-service |
| Phù hợp khi | Cần kết quả ngay để quyết định bước tiếp theo | Cần kết quả ngay, hệ sinh thái REST truyền thống | Việc có thể xử lý trễ, nhiều bên cùng quan tâm 1 sự kiện |
