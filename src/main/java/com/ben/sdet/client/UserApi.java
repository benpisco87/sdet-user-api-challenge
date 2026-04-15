package com.ben.sdet.client;

import com.ben.sdet.common.Result;
import com.ben.sdet.config.ServiceConfig;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.ErrorResponse;
import com.ben.sdet.dto.user.User;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UserApi extends BaseClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public UserApi(ServiceConfig config) {
        super(config);
    }

    // --- POST /users ---
    public Result<User> createUser(CreateUserRequest requestDto) {

        try {
            String body = MAPPER.writeValueAsString(requestDto);

            Request request = baseJsonRequest("/users")
                    .post(RequestBody.create(body, JSON))
                    .build();

            return execute(request, User.class, ErrorResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize CreateUserRequest", e);
        }
    }

    // --- GET /users/{email} ---
    public Result<User> getUser(String email) {

        Request request = baseRequest("/users/" + email)
                .get()
                .build();

        return execute(request, User.class, ErrorResponse.class);
    }
}
