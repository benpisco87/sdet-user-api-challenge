package com.ben.sdet.client;

import com.ben.sdet.config.ServiceConfig;

import okhttp3.Request;

public class UserClient extends BaseClient {

    public UserClient(ServiceConfig config) {
        super(config);
    }

    public Request getUsers() {
        return baseRequest("/users")
                .get()
                .build();
    }

    public Request getUser(String email, String token) {
        return baseRequest("/users/" + email)
                .addHeader("Authorization", token)
                .get()
                .build();
    }

    public Request deleteUser(String email, String token) {
        return baseRequest("/users/" + email)
                .addHeader("Authorization", token)
                .delete()
                .build();
    }
}
