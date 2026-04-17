package com.ben.sdet.tests;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.ben.sdet.common.Result;
import com.ben.sdet.data.TestUserFactory;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.UpdateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.service.UserService;
import com.ben.sdet.validators.UserValidator;

public class UserWriteTests {

    @Test
    public void shouldUpdateUser() {

        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("update", 30);
        UserService.get().createUser(user);

        UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Updated Name")
                .email(user.getEmail())
                .age(40)
                .build();

        Result<User> result = UserService.get().updateUser(user.getEmail(), update);

        UserValidator.validateUpdateSuccess(result, update, softly);
        UserValidator.validateUserExists(user.getEmail(), softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailUpdateNonExistingUser() {

        SoftAssert softly = new SoftAssert();

        UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Ghost User")
                .email("ghost@mail.com")
                .age(30)
                .build();


        Result<User> result = UserService.get().updateUser(update.getEmail(), update);

        UserValidator.validateError(result, 404, "User not found", softly);

        softly.assertAll();
    }

    @Test
    public void shouldDeleteUserWithAuth() {

        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("user", 30);
        UserService.get().createUser(user);

        Result<Void> result = UserService.get().deleteUser(user.getEmail(), "mysecrettoken");

        softly.assertEquals(result.getStatusCode(), 204, "Delete failed");

        UserValidator.validateDeleteSuccess(user.getEmail(), softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailDeleteWithoutAuth() {

        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("user", 30);
        UserService.get().createUser(user);

        Result<Void> result = UserService.get().deleteUser(user.getEmail(), null);

        UserValidator.validateError(result, 401, "Authentication required", softly);
        UserValidator.validateUserExists(user.getEmail(), softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailToCreateDuplicateUser() {

        SoftAssert softly = new SoftAssert();

        CreateUserRequest user = TestUserFactory.defaultUser("dup", 30);

        // First creation → OK
        UserService.get().createUser(user);

        // Second creation → same email
        Result<User> result = UserService.get().createUser(user);

        UserValidator.validateError(result, 409, "Duplicate email", softly);

        softly.assertAll();
    }

    @Test
    public void shouldFailToUpdateWithDuplicateEmail() {

        SoftAssert softly = new SoftAssert();

        // Create two users
        CreateUserRequest user1 = TestUserFactory.defaultUser("user1", 25);
        CreateUserRequest user2 = TestUserFactory.defaultUser("user2", 30);

        UserService.get().createUser(user1);
        UserService.get().createUser(user2);

        // Try to update user1 → email of user2
        UpdateUserRequest update = UpdateUserRequest.builder()
                .name("Updated")
                .email(user2.getEmail()) // ⚠️ duplicate
                .age(40)
                .build();

        Result<User> result = UserService.get().updateUser(user1.getEmail(), update);

        UserValidator.validateError(result, 409, "Duplicate email", softly);

        // Optional but strong: verify original still exists unchanged
        UserValidator.validateUserExists(user1.getEmail(), softly);

        softly.assertAll();
    }
}
