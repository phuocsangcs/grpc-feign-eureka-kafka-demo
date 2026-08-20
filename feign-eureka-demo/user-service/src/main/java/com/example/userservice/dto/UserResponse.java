package com.example.userservice.dto;

/**
 * DTO trả về qua REST - tương đương GetUserResponse message bên gRPC,
 * nhưng ở đây chỉ là 1 POJO/record thường, serialize qua JSON (Jackson)
 * thay vì protobuf binary.
 */
public record UserResponse(
        Integer userId,
        String name,
        String email,
        boolean found
) {
}
