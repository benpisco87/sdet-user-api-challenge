package com.ben.sdet.validators;

import java.util.List;

import org.testng.asserts.SoftAssert;

import com.ben.sdet.common.Result;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.ErrorResponse;
import com.ben.sdet.dto.user.UpdateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.service.UserService;

public class UserValidator {

    // --- Core reusable validation ---
    public static void validateUser(User actual, CreateUserRequest expected, SoftAssert softly) {
        softly.assertNotNull(actual, "User should not be null");

        if (actual != null) {
            softly.assertEquals(actual.getEmail(), expected.getEmail(), "Email");
            softly.assertEquals(actual.getName(), expected.getName(), "Name");
            softly.assertEquals(actual.getAge(), expected.getAge(), "Age");
        }
    }

    // --- POST ---
    public static void validateCreateSuccess(Result<User> result, CreateUserRequest expected) {
        SoftAssert softly = new SoftAssert();

        softly.assertEquals(result.getStatusCode(), 201);
        validateUser(result.getData(), expected, softly);

        softly.assertAll();
    }

    // --- GET by email ---
    public static void validateGetSuccess(Result<User> result, CreateUserRequest expected) {
        SoftAssert softly = new SoftAssert();

        softly.assertEquals(result.getStatusCode(), 200);
        validateUser(result.getData(), expected, softly);

        softly.assertAll();
    }

    // --- LIST ---
    public static void validateUserList(Result<List<User>> result) {
        SoftAssert softly = new SoftAssert();

        softly.assertEquals(result.getStatusCode(), 200);
        softly.assertNotNull(result.getData());

        if (result.getData() != null) {
            for (User user : result.getData()) {
                softly.assertNotNull(user.getEmail());
                softly.assertNotNull(user.getName());
                softly.assertTrue(user.getAge() >= 1 && user.getAge() <= 150);
            }
        }

        softly.assertAll();
    }

    public static void validateUserPresent(List<User> users, CreateUserRequest expected) {
        SoftAssert softly = new SoftAssert();

        boolean found = users.stream()
                .anyMatch(u -> u.getEmail().equals(expected.getEmail()));

        softly.assertTrue(found, "User should exist in list");

        softly.assertAll();
    }

    // --- ERROR ---
    public static void validateError(Result<?> result,
                                 int expectedStatus,
                                 String expectedMessage,
                                 SoftAssert softly) {

        softly.assertEquals(result.getStatusCode(), expectedStatus, "Status code mismatch");

        if (result.isSuccess()) {
            softly.fail("Expected error but got success. Body: " + result.getRawBody());
            return;
        }

        ErrorResponse error;
        try {
            error = result.getError(ErrorResponse.class);
        } catch (Exception e) {
            softly.fail("Failed to map error response. Raw body: " + result.getRawBody());
            return;
        }

        softly.assertNotNull(error, "Error response should not be null");

        if (error != null) {
            softly.assertEquals(error.getError(), expectedMessage, "Error message mismatch");
        }
    }

    public static void validateError(Result<?> result, int expectedStatus, String expectedMessage) {
        SoftAssert softly = new SoftAssert();
        validateError(result, expectedStatus, expectedMessage, softly);
        softly.assertAll();
    }

    public static void validateCreateUserFailure(Result<?> result,
                                             CreateUserRequest request,
                                             int expectedStatus,
                                             String expectedMessage,
                                             SoftAssert softly) {

        // 1. Validate API error
        validateError(result, expectedStatus, expectedMessage, softly);

        // 2. Validate state only if it shouldn't exist
        if (request != null) {
            validateUserNotPersisted(request.getEmail(), softly);
        }
    }

    public static void validateUserNotPersisted(String email, SoftAssert softly) {

        Result<User> get = UserService.get().getUser(email);

        softly.assertTrue(
                get.getStatusCode() == 404 || get.getData() == null,
                "User should NOT exist but was found: " + email
        );
    }

    // ---------- GET SINGLE USER ----------
    public static void validateUser(Result<User> result,
                                    String expectedEmail,
                                    String expectedName,
                                    Integer expectedAge,
                                    SoftAssert softly) {

        softly.assertEquals(result.getStatusCode(), 200, "Status code mismatch");
        softly.assertNotNull(result.getData(), "User should not be null");

        if (result.getData() != null) {
            User u = result.getData();

            softly.assertEquals(u.getEmail(), expectedEmail, "Email mismatch");
            softly.assertEquals(u.getName(), expectedName, "Name mismatch");
            softly.assertEquals(u.getAge(), expectedAge.intValue(), "Age mismatch");
        }
    }

    // ---------- LIST USERS ----------

    public static void validateUserListContains(Result<List<User>> result,
                                                String email,
                                                SoftAssert softly) {

        softly.assertEquals(result.getStatusCode(), 200, "Status code mismatch");

        boolean found = result.getData().stream()
                .anyMatch(u -> u.getEmail().equals(email));

        softly.assertTrue(found, "User not found in list: " + email);
    }

    public static void validateUserListSize(Result<List<User>> result,
                                            int expectedSize,
                                            SoftAssert softly) {

        softly.assertEquals(result.getStatusCode(), 200, "Status code mismatch");
        softly.assertNotNull(result.getData(), "User list should not be null");

        if (result.getData() != null) {
            softly.assertEquals(result.getData().size(), expectedSize, "List size mismatch");
        }
    }

    // ---------- STATE VALIDATION ----------

    public static void validateUserExists(String email, SoftAssert softly) {
        Result<User> result = UserService.get().getUser(email);

        softly.assertEquals(result.getStatusCode(), 200, "Expected user to exist");
        softly.assertNotNull(result.getData(), "User should exist but is null");
    }

    public static void validateUserNotExists(String email, SoftAssert softly) {
        Result<User> result = UserService.get().getUser(email);

        softly.assertEquals(result.getStatusCode(), 404, "Expected user to NOT exist");
    }

    // ---------- UPDATE ----------

    public static void validateUpdateSuccess(Result<User> result,
                                            UpdateUserRequest request,
                                            SoftAssert softly) {

        softly.assertEquals(result.getStatusCode(), 200, "Status code mismatch");
        softly.assertNotNull(result.getData(), "Updated user should not be null");

        if (result.getData() != null) {
            softly.assertEquals(result.getData().getName(), request.getName(), "Name mismatch");
            softly.assertEquals(result.getData().getAge(), request.getAge(), "Age mismatch");
            softly.assertEquals(result.getData().getEmail(), request.getEmail(), "Email mismatch");
        }
    }

    // ---------- DELETE ----------

    public static void validateDeleteSuccess(String email, SoftAssert softly) {
        validateUserNotExists(email, softly);
    }

}