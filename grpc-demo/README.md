# Demo: 2 microservice Spring Boot giao tiếp qua gRPC

Dùng thư viện `net.devh:grpc-spring-boot-starter` (nay là grpc-ecosystem/grpc-spring)
để tích hợp gRPC vào Spring Boot mà không cần tự viết `io.grpc.Server`/`ManagedChannel`
thủ công như gRPC Java thuần.

## Cấu trúc
```
grpc-java-demo/
├── user-service/                 # microservice #1 - gRPC server, port 9091
│   ├── pom.xml
│   └── src/main/
│       ├── proto/user.proto
│       ├── java/.../UserServiceApplication.java
│       └── java/.../UserServiceImpl.java   # @GrpcService
└── order-service/                # microservice #2 - vừa server (9092) vừa client gọi user-service
    ├── pom.xml
    └── src/main/
        ├── proto/order.proto
        ├── proto/user.proto      # copy để sinh client stub
        ├── java/.../OrderServiceApplication.java
        ├── java/.../OrderServiceImpl.java  # @GrpcService + @GrpcClient
        └── java/.../OrderClientTest.java   # client test gọi thử
```

## Yêu cầu
- Java 17+, Maven 3.8+
- `protobuf-maven-plugin` sẽ tự tải `protoc` binary phù hợp OS khi build (cần mạng)

## Chạy demo
```bash
# Terminal 1 - chạy UserService
cd user-service
mvn spring-boot:run

# Terminal 2 - chạy OrderService
cd order-service
mvn spring-boot:run

# Terminal 3 - gọi thử qua client test
cd order-service
mvn compile exec:java -Dexec.mainClass="com.example.orderservice.OrderClientTest"
# (nếu chưa có exec-maven-plugin, có thể chạy trực tiếp OrderClientTest.main() từ IDE)
```

Kết quả mong đợi:
```
Kết quả từ OrderService:
  order_id  = 1000
  status    = SUCCESS
  user_name = Nguyen Van A
```

## Cơ chế hoạt động
- `@GrpcService` trên `UserServiceImpl` / `OrderServiceImpl`: tự động đăng ký
  class đó làm 1 gRPC service khi Spring context khởi động, lắng nghe ở port
  cấu hình trong `grpc.server.port` (application.yml).
- `@GrpcClient("user-service")` trên field `UserServiceBlockingStub` trong
  `OrderServiceImpl`: Spring tự tạo `ManagedChannel` + stub trỏ tới địa chỉ
  cấu hình trong `grpc.client.user-service.address`, rồi inject sẵn - không
  cần tự quản lý channel như code Java gRPC thuần.
- protobuf-maven-plugin tự chạy `protoc` sinh code Java từ file `.proto`
  trong `src/main/proto` mỗi lần `mvn compile`.

## Lưu ý
- `order-service` cần 1 bản copy `user.proto` để sinh ra stub gọi sang
  `UserService`. Trong dự án thật, nên tách các file `.proto` ra 1 module/
  artifact dùng chung (ví dụ publish lên Nexus/Artifactory nội bộ) thay vì
  copy tay để tránh 2 bên proto bị lệch nhau.
- Trong Kubernetes, đổi `grpc.client.user-service.address` sang dạng DNS,
  ví dụ `dns:///user-service.default.svc.cluster.local:9091`.
- Đây là code chạy insecure (`negotiationType: PLAINTEXT`) - production nên
  bật TLS.
