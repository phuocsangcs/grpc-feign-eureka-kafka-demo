package com.example.userservice.controller;

import com.example.userservice.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Đây chính là microservice #1 - tương đương UserServiceImpl bên gRPC,
 * nhưng ở đây là 1 @RestController bình thường, expose qua HTTP + JSON.
 * order-service sẽ gọi vào endpoint này thông qua Feign, KHÔNG gọi trực tiếp.
 */
@RestController
public class UserController {

    private final Map<Integer, String[]> fakeUserDb = new HashMap<>();

    public UserController() {
        fakeUserDb.put(1, new String[]{"Nguyen Van A", "a.nguyen@example.com"});
        fakeUserDb.put(2, new String[]{"Tran Thi B", "b.tran@example.com"});
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable("id") Integer id) {
        System.out.println("[UserService] Nhận request GET /users/" + id);

        String[] user = fakeUserDb.get(id);
        if (user == null) {
            return new UserResponse(id, null, null, false);
        }
        return new UserResponse(id, user[0], user[1], true);
    }
}
