package com.example.orderservice;

import com.example.grpcdemo.order.CreateOrderRequest;
import com.example.grpcdemo.order.CreateOrderResponse;
import com.example.grpcdemo.order.OrderServiceGrpc;
import com.example.grpcdemo.user.GetUserRequest;
import com.example.grpcdemo.user.GetUserResponse;
import com.example.grpcdemo.user.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Đây chính là microservice #2. Nó vừa:
 *  - LÀ SERVER: implement CreateOrder, nhận request từ bên ngoài
 *  - LÀ CLIENT: dùng @GrpcClient để tự tạo 1 stub gọi sang UserService
 *
 * @GrpcClient("user-service") sẽ đọc config "grpc.client.user-service.*"
 * trong application.yml để biết địa chỉ (host:port) của UserService, rồi
 * Spring tự inject 1 blocking stub đã sẵn sàng dùng - không cần tự quản
 * lý ManagedChannel như code Java gRPC thuần.
 */
@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    private final AtomicInteger orderIdCounter = new AtomicInteger(1000);

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        System.out.println("[OrderService] Nhận request CreateOrder(user_id="
                + request.getUserId() + ", product=" + request.getProduct() + ")");

        GetUserResponse userResponse;
        try {
            // ---- Gọi gRPC sang UserService ----
            GetUserRequest userRequest = GetUserRequest.newBuilder()
                    .setUserId(request.getUserId())
                    .build();
            userResponse = userServiceStub.getUser(userRequest);
            // ------------------------------------
        } catch (StatusRuntimeException e) {
            // UserService không phản hồi / down / timeout
            System.err.println("[OrderService] Gọi UserService thất bại: " + e.getStatus());
            responseObserver.onNext(CreateOrderResponse.newBuilder()
                    .setStatus("FAILED")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        if (!userResponse.getFound()) {
            responseObserver.onNext(CreateOrderResponse.newBuilder()
                    .setStatus("FAILED")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        int orderId = orderIdCounter.getAndIncrement();
        System.out.println("[OrderService] Tạo order #" + orderId + " cho user '" + userResponse.getName() + "'");

        CreateOrderResponse response = CreateOrderResponse.newBuilder()
                .setOrderId(orderId)
                .setStatus("SUCCESS")
                .setUserName(userResponse.getName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
