package com.ben.sdet.tests;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.ben.sdet.common.Result;
import com.ben.sdet.data.TestUserFactory;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.service.UserService;
import com.ben.sdet.validators.UserValidator;

public class UserReadTests {

    @Test
    public void shouldReturnUserByEmail() {

        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("user", 30);

        UserService.get().createUser(user);

        Result<User> result = UserService.get().getUser(user.getEmail());

        UserValidator.validateUser(
                result,
                user.getEmail(),
                user.getName(),
                user.getAge(),
                softly
        );

        softly.assertAll();
    }

    @Test
    public void shouldReturn404ForNonExistingUser() {

        SoftAssert softly = new SoftAssert();

        Result<User> result = UserService.get().getUser("notfound@mail.com");

        UserValidator.validateError(result, 404, "User not found", softly);

        softly.assertAll();
    }

    @Test
    public void shouldIncreaseListAfterMultipleUsers() {

        SoftAssert softly = new SoftAssert();

        CreateUserRequest user1 = TestUserFactory.defaultUser("user1", 25);
        CreateUserRequest user2 = TestUserFactory.defaultUser("user2", 30);

        UserService.get().createUser(user1);
        UserService.get().createUser(user2);

        Result<List<User>> list = UserService.get().listUsers();

        UserValidator.validateUserListSize(list, 2, softly);
        UserValidator.validateUserListContains(list, user1.getEmail(), softly);
        UserValidator.validateUserListContains(list, user2.getEmail(), softly);

        softly.assertAll();
    }
}
