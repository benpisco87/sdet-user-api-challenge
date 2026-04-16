package com.ben.sdet.data;

import com.ben.sdet.dto.user.CreateUserRequest;

import utils.data.TestDataUtils;

public class TestUserFactory {
    
    private TestUserFactory() {}

    public static CreateUserRequest defaultUser(String baseEmail, int age) {
        return CreateUserRequest.builder()
                .name("Test User")
                .email(TestDataUtils.uniqueEmail(baseEmail))
                .age(age)
                .build();
    }

}
