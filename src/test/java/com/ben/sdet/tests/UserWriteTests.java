package com.ben.sdet.tests;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.ben.sdet.common.Result;
import com.ben.sdet.data.TestUserFactory;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.UpdateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.logging.LoggerUtils;
import com.ben.sdet.service.UserService;
import com.ben.sdet.validators.UserValidator;

public class UserWriteTests {

    @Test
    public void shouldUpdateUser() {

        LoggerUtils.info("### Step 1 - Creating user for update test");
        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("update", 30);
        UserService.get().createUser(user);

        LoggerUtils.info("### Step 2 - Sending update request");
        UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Updated Name")
                .email(user.getEmail())
                .age(40)
                .build();

        Result<User> result = UserService.get().updateUser(user.getEmail(), update);

        LoggerUtils.info("### Step 3 - Validating update success and persistence");
        UserValidator.validateUpdateSuccess(result, update, softly);
        UserValidator.validateUserExists(user.getEmail(), softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailUpdateNonExistingUser() {

        LoggerUtils.info("### Step 1 - Preparing update request for missing user");
        SoftAssert softly = new SoftAssert();

        UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Ghost User")
                .email("ghost@mail.com")
                .age(30)
                .build();

        LoggerUtils.info("### Step 2 - Sending update request for non-existing user");
        Result<User> result = UserService.get().updateUser(update.getEmail(), update);

        LoggerUtils.info("### Step 3 - Validating 404 not found response");
        UserValidator.validateError(result, 404, "User not found", softly);

        softly.assertAll();
    }

    @Test
    public void shouldDeleteUserWithAuth() {

        LoggerUtils.info("### Step 1 - Creating user to delete with auth");
        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("user", 30);
        UserService.get().createUser(user);

        LoggerUtils.info("### Step 2 - Deleting user with auth token");
        Result<Void> result = UserService.get().deleteUser(user.getEmail(), "mysecrettoken");

        softly.assertEquals(result.getStatusCode(), 204, "Delete failed");

        LoggerUtils.info("### Step 3 - Validating delete success and absence");
        UserValidator.validateDeleteSuccess(user.getEmail(), softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailDeleteWithoutAuth() {

        LoggerUtils.info("### Step 1 - Creating user for unauthenticated delete test");
        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("user", 30);
        UserService.get().createUser(user);

        LoggerUtils.info("### Step 2 - Sending delete request without auth header");
        Result<Void> result = UserService.get().deleteUser(user.getEmail(), null);

        LoggerUtils.info("### Step 3 - Validating authentication failure and original user remains");
        UserValidator.validateError(result, 401, "Authentication required", softly);
        UserValidator.validateUserExists(user.getEmail(), softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailToCreateDuplicateUser() {

        LoggerUtils.info("### Step 1 - Creating initial user for duplicate create test");
        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("dup", 30);

        // First creation → OK
        UserService.get().createUser(user);

        LoggerUtils.info("### Step 2 - Sending duplicate create request");
        Result<User> result = UserService.get().createUser(user);

        LoggerUtils.info("### Step 3 - Validating duplicate error response");
        UserValidator.validateError(result, 409, "Duplicate email", softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailToUpdateWithDuplicateEmail() {

        LoggerUtils.info("### Step 1 - Creating two users for duplicate-email update test");
        SoftAssert softly = new SoftAssert();

        // Create two users
        CreateUserRequest user1 = TestUserFactory.defaultUser("user1", 25);
        CreateUserRequest user2 = TestUserFactory.defaultUser("user2", 30);

        UserService.get().createUser(user1);
        UserService.get().createUser(user2);

        LoggerUtils.info("### Step 2 - Sending update request with duplicate email");
        // Try to update user1 → email of user2
        UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Updated")
                .email(user2.getEmail()) // ⚠️ duplicate
                .age(40)
                .build();

        Result<User> result = UserService.get().updateUser(user1.getEmail(), update);

        LoggerUtils.info("### Step 3 - Validating duplicate email error and original user still exists");
        UserValidator.validateError(result, 409, "Duplicate email", softly);

        // Optional but strong: verify original still exists unchanged
        UserValidator.validateUserExists(user1.getEmail(), softly);

        softly.assertAll();
    }
}
