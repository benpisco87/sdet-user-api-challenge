package com.ben.sdet.client;

import com.ben.sdet.config.ServiceConfig;
import com.ben.sdet.model.ErrorResponse;
import com.ben.sdet.model.Result;
import com.ben.sdet.model.User;
import okhttp3.Request;

public class UserApi extends BaseClient {

    public UserApi(ServiceConfig config) {
        super(config);
    }

    public Result<User[]> getUsers() {
        Request request = baseRequest("/users")
                .get()
                .build();

        return execute(request, User[].class, ErrorResponse.class);
    }

    public Result<User> getUser(String email, String token) {
        Request request = baseRequest("/users/" + email)
                .addHeader("Authorization", token)
                .get()
                .build();

        return execute(request, User.class, ErrorResponse.class);
    }

    public Result<Object> deleteUser(String email, String token) {
        Request request = baseRequest("/users/" + email)
                .addHeader("Authorization", token)
                .delete()
                .build();

        return execute(request, Object.class, ErrorResponse.class);
    }
}
