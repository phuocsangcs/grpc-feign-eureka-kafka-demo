package com.example.orderservice;

import com.example.grpcdemo.order.CreateOrderRequest;
import com.example.grpcdemo.order.CreateOrderResponse;
import com.example.grpcdemo.order.OrderServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Client test độc lập, đóng vai trò "bên ngoài" (vd: API Gateway) gọi
 * vào OrderService. Chạy tách biệt, KHÔNG phải 1 phần của Spring context.
 *
 * Luồng chạy thực tế:
 *   OrderClientTest --gRPC--> OrderService (9092) --gRPC--> UserService (9091)
 *
 * Chạy bằng: mvn compile exec:java -Dexec.mainClass=com.example.orderservice.OrderClientTest
 *! .\mvnw.cmd -f order-service\pom.xml compile exec:java "-Dexec.mainClass=com.example.orderservice.OrderClientTest"
 * (cần thêm exec-maven-plugin, hoặc chạy trực tiếp từ IDE)
 */
public class OrderClientTest {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9092)
                .usePlaintext()
                .build();

        OrderServiceGrpc.OrderServiceBlockingStub stub = OrderServiceGrpc.newBlockingStub(channel);

        CreateOrderRequest request = CreateOrderRequest.newBuilder()
                .setUserId(1)
                .setProduct("Laptop Dell XPS")
                .build();

        CreateOrderResponse response = stub.createOrder(request);

        System.out.println("Kết quả từ OrderService:");
        System.out.println("  order_id  = " + response.getOrderId());
        System.out.println("  status    = " + response.getStatus());
        System.out.println("  user_name = " + response.getUserName());

        channel.shutdown();
    }
}
