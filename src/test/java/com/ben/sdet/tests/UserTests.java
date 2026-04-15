package com.ben.sdet.tests;

import com.ben.sdet.model.Result;
import com.ben.sdet.service.UserService;
import com.ben.sdet.dataproviders.UserDataProvider;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.User;

import org.testng.annotations.Test;

public class UserTests {

    private static final String TOKEN = "mysecrettoken";

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldCreateAndGetUser(CreateUserRequest user, String label) {
        Result<User> create = UserService.get().createUser(user);

        assert create.getStatusCode() == 201 : label;
        assert create.getData() != null : label;

        Result<User> get = UserService.get()
                .getUser(user.getEmail());

        assert get.getStatusCode() == 200 : label;
        assert get.getData().getEmail().equals(user.getEmail());
        assert get.getData().getName().equals(user.getName());
    }

    @Test(dataProvider = "invalidUsers", dataProviderClass = UserDataProvider.class)
    public void shouldFailToCreateInvalidUser(CreateUserRequest user, String label) {
        Result<User> result = UserService.get().createUser(user);

        assert result.getStatusCode() == 400 : label;
    }
}
