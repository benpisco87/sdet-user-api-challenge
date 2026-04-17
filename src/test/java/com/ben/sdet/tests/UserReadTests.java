package com.ben.sdet.tests;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.ben.sdet.common.Result;
import com.ben.sdet.data.TestUserFactory;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.logging.LoggerUtils;
import com.ben.sdet.service.UserService;
import com.ben.sdet.validators.UserValidator;

public class UserReadTests {

    @Test
    public void shouldReturnUserByEmail() {

        LoggerUtils.info("### Step 1 - Creating user for read-by-email test");
        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("user", 30);

        UserService.get().createUser(user);

        LoggerUtils.info("### Step 2 - Retrieving user by email");
        Result<User> result = UserService.get().getUser(user.getEmail());

        LoggerUtils.info("### Step 3 - Validating retrieved user details");
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

        LoggerUtils.info("### Step 1 - Retrieving non-existing user by email");
        SoftAssert softly = new SoftAssert();

        Result<User> result = UserService.get().getUser("notfound@mail.com");

        LoggerUtils.info("### Step 2 - Validating 404 response");
        UserValidator.validateError(result, 404, "User not found", softly);

        softly.assertAll();
    }

    @Test
    public void shouldIncreaseListAfterMultipleUsers() {

        LoggerUtils.info("### Step 1 - Creating first test user");
        SoftAssert softly = new SoftAssert();

        CreateUserRequest user1 = TestUserFactory.defaultUser("user1", 25);
        CreateUserRequest user2 = TestUserFactory.defaultUser("user2", 30);

        UserService.get().createUser(user1);

        LoggerUtils.info("### Step 2 - Creating second test user");
        UserService.get().createUser(user2);

        LoggerUtils.info("### Step 3 - Listing users and validating result");
        Result<List<User>> list = UserService.get().listUsers();

        UserValidator.validateUserListContains(list, user1.getEmail(), softly);
        UserValidator.validateUserListContains(list, user2.getEmail(), softly);

        softly.assertAll();
    }
}
