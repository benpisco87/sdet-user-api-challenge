package com.ben.sdet.client;

import java.util.List;

import com.ben.sdet.common.Result;
import com.ben.sdet.config.ServiceConfig;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.ErrorResponse;
import com.ben.sdet.dto.user.UpdateUserRequest;
import com.ben.sdet.dto.user.User;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UserApi extends BaseClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public UserApi(ServiceConfig config) {
        super(config);
    }

    // =========================
    // POST /users
    // =========================
    public Result<User> createUser(CreateUserRequest requestDto) {

        String body;
        try {
            body = MAPPER.writeValueAsString(requestDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize CreateUserRequest: " + requestDto, e);
        }

        Request request = baseJsonRequest("/users")
                .post(RequestBody.create(body, JSON))
                .build();

        return execute(request, User.class, ErrorResponse.class);
    }

    // =========================
    // GET /users/{email}
    // =========================
    public Result<User> getUser(String email) {

        Request request = baseRequest("/users/" + email)
                .get()
                .build();

        return execute(request, User.class, ErrorResponse.class);
    }

    // =========================
    // GET /users
    // =========================
    public Result<List<User>> listUsers() {

        Request request = baseRequest("/users")
                .get()
                .build();

        return executeList(request, User.class, ErrorResponse.class);
    }

    // =========================
    // PUT /users/{email}
    // =========================
    public Result<User> updateUser(String email, UpdateUserRequest requestDto) {

        String body;
        try {
            body = MAPPER.writeValueAsString(requestDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize UpdateUserRequest: " + requestDto, e);
        }

        Request request = baseJsonRequest("/users/" + email)
                .put(RequestBody.create(body, JSON))
                .build();

        return execute(request, User.class, ErrorResponse.class);
    }

    // =========================
    // DELETE /users/{email}
    // =========================
    public Result<Void> deleteUser(String email, String token) {

        Request request = baseRequest("/users/" + email)
                .addHeader("Authentication", token)
                .delete()
                .build();

        return execute(request, Void.class, ErrorResponse.class);
    }
}
