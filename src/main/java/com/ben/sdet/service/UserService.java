package com.ben.sdet.service;

import com.ben.sdet.client.UserApi;
import com.ben.sdet.common.Result;
import com.ben.sdet.config.ConfigProvider;
import com.ben.sdet.config.UserServiceConfig;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.utils.RetryUtil;

public class UserService {

    private static UserService instance;
    private final UserApi userApi;

    private UserService(UserApi userApi) {
        this.userApi = userApi;
    }

    public static UserService get() {
    if (instance == null) {
        instance = new UserService(new UserApi(new UserServiceConfig()));
    }
        return instance;
    }

    public static UserService getMocked(UserApi mockApi) {
        return new UserService(mockApi);
    }

    public User getUserOrThrow(String email) {
        Result<User> result = RetryUtil.execute(() -> userApi.getUser(email), 2);

        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to get user: " + result.getRawBody());
        }

        return result.getData();
    }

    public Result<User> getUser(String email) {
        return userApi.getUser(email);
    }
    public Result<User> createUser(CreateUserRequest user) {
        return userApi.createUser(user);
    }
}
