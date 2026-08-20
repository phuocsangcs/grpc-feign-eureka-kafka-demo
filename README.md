# Microservice Communication Patterns — So sánh 3 phương pháp

Repo này chứa 3 cách khác nhau để 2 microservice (`user-service` và
`order-service`) giao tiếp với nhau, cùng chạy trên Spring Boot, cùng
1 bài toán demo (tạo order cần biết thông tin user) nhưng giải quyết
theo 3 hướng khác nhau — để so sánh trực tiếp ưu/nhược điểm của từng
cách tiếp cận. Chi tiết setup, code, cách chạy nằm trong README riêng
của từng thư mục.

## Cấu trúc

| Thư mục | Phương pháp | Mô hình |
|---|---|---|
| `grpc-demo/` | gRPC | Đồng bộ (RPC), HTTP/2, protobuf |
| `feign-eureka-demo/` | REST + Feign + Eureka | Đồng bộ (REST/JSON), service discovery |
| `kafka-demo/` | Apache Kafka | Bất đồng bộ (pub/sub, event-driven) |

## So sánh

| | gRPC | REST (Feign + Eureka) | Kafka |
|---|---|---|---|
| **Mô hình** | Request-response đồng bộ | Request-response đồng bộ | Publish-subscribe bất đồng bộ |
| **Ưu điểm** | Nhanh, gọn nhẹ (protobuf binary), contract rõ ràng và type-safe, hỗ trợ streaming tốt | Đơn giản, dễ debug (curl/Postman đọc được ngay), hệ sinh thái REST phổ biến, dễ expose cho client bên ngoài | Decoupled hoàn toàn giữa các service, chịu lỗi tốt (message không mất khi consumer down), dễ thêm consumer mới mà không sửa code bên gửi, chịu tải lớn tốt |
| **Nhược điểm** | Cần bước sinh code từ `.proto`, khó debug bằng mắt thường (payload nhị phân), browser không gọi thẳng được | Chậm hơn gRPC (JSON + HTTP/1.1), cần thêm hạ tầng service registry, vẫn phụ thuộc uptime lẫn nhau (lỗi dây chuyền) | Hạ tầng phức tạp hơn (phải vận hành Kafka cluster), eventual consistency (không có kết quả ngay lập tức), khó theo dõi thứ tự xử lý toàn hệ thống |
| **Phù hợp khi** | Giao tiếp nội bộ giữa các service, cần độ trễ thấp, hệ thống lớn nhiều service tự kiểm soát cả 2 đầu | API public cho frontend/mobile, hệ thống vừa và nhỏ, team quen REST, cần debug/tích hợp nhanh | Xử lý sự kiện có thể trễ, nhiều service cùng quan tâm 1 sự kiện, pipeline dữ liệu lớn, cần khả năng chịu lỗi cao |

## Chọn nhanh

- Cần **kết quả ngay** để quyết định bước tiếp theo → gRPC hoặc REST.
- Cần **hiệu năng cao, nội bộ giữa các service** → gRPC.
- Cần **dễ debug, tích hợp rộng, expose ra ngoài** → REST (Feign + Eureka).
- Việc **có thể xử lý trễ**, hoặc **nhiều bên cùng quan tâm 1 sự kiện** → Kafka.

Trong thực tế, hầu hết hệ thống lớn dùng **kết hợp cả 3**: REST cho API public, gRPC cho giao tiếp nội bộ cần tốc độ, Kafka cho các luồng sự kiện bất đồng bộ — không nhất thiết phải chọn 1 và bỏ 2 cái còn lại.
