# Demo: 2 microservice giao tiếp qua REST + Feign + Eureka

Bản REST truyền thống tương ứng với bản gRPC đã làm trước đó - cùng
domain (UserService / OrderService) để dễ so sánh trực tiếp 2 cách tiếp cận.

## Cấu trúc
```
feign-eureka-demo/
├── eureka-server/     # Service registry, chạy port 8761
├── user-service/      # microservice #1 - REST server thuần, port 8081
└── order-service/      # microservice #2 - REST server + Feign client, port 8082
```

## Yêu cầu
- Java 17+, Maven 3.8+ (cần mạng để tải Spring Cloud dependencies)

## Chạy demo (3 terminal, đúng thứ tự)
```bash
# Terminal 1 - Eureka Server PHẢI chạy trước
cd eureka-server
mvn spring-boot:run
# Mở http://localhost:8761 để xem dashboard registry

# Terminal 2 - UserService (chờ ~10s sau khi Eureka lên, để tự đăng ký)
cd user-service
mvn spring-boot:run

# Terminal 3 - OrderService
cd order-service
mvn spring-boot:run
```

## Test thử
```bash
# Gọi trực tiếp UserService
curl http://localhost:8081/users/1

# Gọi OrderService, bên trong nó tự gọi sang UserService qua Feign + Eureka
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "product": "Laptop Dell XPS"}'
```

Kết quả mong đợi:
```json
{"orderId":1000,"status":"SUCCESS","userName":"Nguyen Van A"}
```

## So sánh nhanh với bản gRPC

| | gRPC | Feign + Eureka |
|---|---|---|
| Contract | file `.proto`, sinh code lúc build | Java interface + annotation, không cần bước sinh code riêng |
| "Stub" phía client | do `protoc` sinh sẵn, cụ thể theo `.proto` | Spring tự tạo dynamic proxy lúc runtime từ interface bạn viết |
| Tìm địa chỉ service khác | khai tĩnh trong `application.yml` (`grpc.client.*.address`) | tra cứu động qua Eureka (`@FeignClient(name = "...")`) |
| Định dạng dữ liệu | protobuf (binary, nén gọn) | JSON (text, dễ đọc/debug hơn) |
| Giao thức | HTTP/2 | HTTP/1.1 (mặc định) |
| Thêm 1 instance mới của user-service | tự cấu hình load balancing riêng | Eureka + Feign tự load balance qua các instance đã đăng ký |

## Cơ chế hoạt động
1. `eureka-server` khởi động, là 1 "danh bạ" trống.
2. `user-service` khởi động, tự gửi heartbeat đăng ký "tôi là user-service, đang chạy tại host:8081" vào Eureka.
3. `order-service` khởi động. Interface `UserClient` có `@FeignClient(name = "user-service")` được `@EnableFeignClients` quét thấy, Spring tạo 1 proxy implement interface này.
4. Khi code gọi `userClient.getUser(1)`, proxy đó:
   - Hỏi Eureka: "user-service" đang chạy ở đâu?
   - Build request HTTP GET tới `http://<host>:<port>/users/1`
   - Gửi đi, nhận response JSON, deserialize thành `UserResponse`
   - Trả về y như 1 lời gọi hàm local
5. Nếu có nhiều instance `user-service` cùng đăng ký 1 tên, Feign (qua Spring Cloud LoadBalancer) tự round-robin giữa các instance - đây là thứ bạn phải tự làm thủ công nếu dùng `RestTemplate` gọi địa chỉ cứng.
