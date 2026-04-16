package com.ben.sdet.validators;

import java.util.List;

import org.testng.asserts.SoftAssert;

import com.ben.sdet.common.Result;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.ErrorResponse;
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

}