package com.ben.sdet.tests;

import com.ben.sdet.service.UserService;
import com.ben.sdet.validators.UserValidator;

import ch.qos.logback.classic.Logger;

import com.ben.sdet.common.Result;
import com.ben.sdet.data.TestUserFactory;
import com.ben.sdet.data.UserDataProvider;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.User;
import com.ben.sdet.logging.LoggerUtils;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class UserTests {

    private final UserService service = UserService.get();

    // =========================
    // POST /users
    // =========================

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldCreateUser(CreateUserRequest user, String label) {
        LoggerUtils.info("### Step 1 - Creating a user for test: " + label);
        Result<User> result = service.createUser(user);
        LoggerUtils.info("### Step 2 - Validating the response");
        UserValidator.validateCreateSuccess(result, user);
    }

    @Test(dataProvider = "invalidUsers", dataProviderClass = UserDataProvider.class)
    public void shouldFailToCreateUser(CreateUserRequest user,
                                       String label,
                                       int expectedStatus,
                                       String expectedError) {

        LoggerUtils.info("### Step 1 - Sending invalid create request for test: " + label);
        SoftAssert softly = new SoftAssert();

        Result<User> result = UserService.get().createUser(user);

        LoggerUtils.info("### Step 2 - Validating failure response");
        UserValidator.validateCreateUserFailure(
                result,
                user,
                expectedStatus,
                expectedError,
                softly
        );

        softly.assertAll();
    }

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldNotAllowDuplicateUsers(CreateUserRequest user, String label) {

        LoggerUtils.info("### Step 1 - Creating initial user for duplicate test: " + label);
        service.createUser(user);

        LoggerUtils.info("### Step 2 - Creating duplicate user to verify conflict");
        Result<User> duplicate = service.createUser(user);

        LoggerUtils.info("### Step 3 - Validating duplicate error response");
        UserValidator.validateError(duplicate, 409, "Duplicate email");
    }

    // =========================
    // FLOW TESTS
    // =========================

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldCreateAndGetUser(CreateUserRequest user, String label) {

        LoggerUtils.info("### Step 1 - Creating user for GET test: " + label);
        service.createUser(user);

        LoggerUtils.info("### Step 2 - Retrieving user by email");
        Result<User> get = service.getUser(user.getEmail());

        LoggerUtils.info("### Step 3 - Validating retrieved user");
        UserValidator.validateGetSuccess(get, user);
    }

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldCreateUserAndAppearInList(CreateUserRequest user, String label) {

        LoggerUtils.info("### Step 1 - Creating user for list membership test: " + label);
        service.createUser(user);

        LoggerUtils.info("### Step 2 - Listing users and validating presence");
        Result<List<User>> result = service.listUsers();

        UserValidator.validateUserList(result);
        UserValidator.validateUserPresent(result.getData(), user);
    }

    @Test
    public void shouldIncreaseListAfterMultipleUsers() {

        LoggerUtils.info("### Step 1 - Creating first user for list growth test");
        CreateUserRequest user1 = TestUserFactory.defaultUser("u1@mail.com", 25);
        CreateUserRequest user2 = TestUserFactory.defaultUser("u2@mail.com", 30);

        service.createUser(user1);
        LoggerUtils.info("### Step 2 - Listing users after first creation");
        Result<List<User>> first = service.listUsers();

        LoggerUtils.info("### Step 3 - Creating second user");
        service.createUser(user2);
        LoggerUtils.info("### Step 4 - Listing users after second creation");
        Result<List<User>> second = service.listUsers();

        UserValidator.validateUserList(first);
        UserValidator.validateUserList(second);

        LoggerUtils.info("### Step 5 - Validating user count growth");
        assert second.getData().size() >= first.getData().size() + 1;
    }

    // =========================
    // EDGE CASES
    // =========================

    @Test
    public void shouldFailWithNullBody() {

        LoggerUtils.info("### Step 1 - Sending create request with null body");
        Result<User> result = service.createUser(null);

        LoggerUtils.info("### Step 2 - Validating invalid JSON body error");
        UserValidator.validateError(result, 400, "Invalid JSON body");
    }

    @Test
    public void shouldNotLeakUsersAcrossEnvironments() {

        LoggerUtils.info("### Step 1 - Starting environment isolation test");
        SoftAssert softly = new SoftAssert();

        // current env (comes from -Denv)
        String currentEnvStr = System.getProperty("env", "dev");

        // opposite env
        String otherEnv = currentEnvStr.equals("dev") ? "prod" : "dev";

        LoggerUtils.info("### Step 2 - Current env: " + currentEnvStr + ", verifying against other env: " + otherEnv);
        UserService currentService = UserService.get(currentEnvStr);
        UserService otherService = UserService.get(otherEnv);

        CreateUserRequest user = TestUserFactory.defaultUser("env@mail.com", 30);

        LoggerUtils.info("### Step 3 - Creating user in current env");
        currentService.createUser(user);

        LoggerUtils.info("### Step 4 - Attempting to retrieve user from other env");
        Result<User> result = otherService.getUser(user.getEmail());

        LoggerUtils.info("### Step 5 - Validating user is not visible in opposite env");
        UserValidator.validateError(result, 404, "User not found", softly);

        softly.assertAll();
    }
}
