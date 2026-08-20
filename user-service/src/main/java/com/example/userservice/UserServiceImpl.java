package com.example.userservice;

import com.example.grpcdemo.user.GetUserRequest;
import com.example.grpcdemo.user.GetUserResponse;
import com.example.grpcdemo.user.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashMap;
import java.util.Map;

/**
 * Đây chính là microservice #1: chỉ implement 1 API GetUser.
 * Annotation @GrpcService khiến grpc-spring-boot-starter tự động
 * đăng ký class này như 1 gRPC service khi app khởi động (không cần
 * tự viết code io.grpc.Server như bên Python).
 */
@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    // Giả lập database user
    private final Map<Integer, String[]> fakeUserDb = new HashMap<>();

    public UserServiceImpl() {
        fakeUserDb.put(1, new String[]{"Nguyen Van A", "a.nguyen@example.com"});
        fakeUserDb.put(2, new String[]{"Tran Thi B", "b.tran@example.com"});
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        System.out.println("[UserService] Nhận request GetUser(user_id=" + request.getUserId() + ")");

        String[] user = fakeUserDb.get(request.getUserId());

        GetUserResponse response;
        if (user == null) {
            response = GetUserResponse.newBuilder().setFound(false).build();
        } else {
            response = GetUserResponse.newBuilder()
                    .setUserId(request.getUserId())
                    .setName(user[0])
                    .setEmail(user[1])
                    .setFound(true)
                    .build();
        }

        // onNext + onCompleted: đây là cách trả response cho unary RPC trong gRPC Java
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
