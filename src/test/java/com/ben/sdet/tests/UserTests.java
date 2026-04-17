package com.ben.sdet.tests;

import com.ben.sdet.service.UserService;
import com.ben.sdet.validators.UserValidator;
import com.ben.sdet.common.Result;
import com.ben.sdet.data.TestUserFactory;
import com.ben.sdet.data.UserDataProvider;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.dto.user.User;

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

        Result<User> result = service.createUser(user);

        UserValidator.validateCreateSuccess(result, user);
    }

    @Test(dataProvider = "invalidUsers", dataProviderClass = UserDataProvider.class)
    public void shouldFailToCreateUser(CreateUserRequest user,
                                       String label,
                                       int expectedStatus,
                                       String expectedError) {

        SoftAssert softly = new SoftAssert();

        Result<User> result = UserService.get().createUser(user);

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

        service.createUser(user);

        Result<User> duplicate = service.createUser(user);

        UserValidator.validateError(duplicate, 409, "Duplicate email");
    }

    // =========================
    // FLOW TESTS
    // =========================

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldCreateAndGetUser(CreateUserRequest user, String label) {

        service.createUser(user);

        Result<User> get = service.getUser(user.getEmail());

        UserValidator.validateGetSuccess(get, user);
    }

    @Test(dataProvider = "validUsers", dataProviderClass = UserDataProvider.class)
    public void shouldCreateUserAndAppearInList(CreateUserRequest user, String label) {

        service.createUser(user);

        Result<List<User>> result = service.listUsers();

        UserValidator.validateUserList(result);
        UserValidator.validateUserPresent(result.getData(), user);
    }

    @Test
    public void shouldIncreaseListAfterMultipleUsers() {

        CreateUserRequest user1 = TestUserFactory.defaultUser("u1@mail.com", 25);
        CreateUserRequest user2 = TestUserFactory.defaultUser("u2@mail.com", 30);

        service.createUser(user1);
        Result<List<User>> first = service.listUsers();

        service.createUser(user2);
        Result<List<User>> second = service.listUsers();

        UserValidator.validateUserList(first);
        UserValidator.validateUserList(second);

        assert second.getData().size() >= first.getData().size() + 1;
    }

    // =========================
    // EDGE CASES
    // =========================

    @Test
    public void shouldFailWithNullBody() {

        Result<User> result = service.createUser(null);

        UserValidator.validateError(result, 400, "Invalid JSON body");
    }

    @Test
    public void shouldNotLeakUsersAcrossEnvironments() {

        SoftAssert softly = new SoftAssert();

        // current env (comes from -Denv)
        String currentEnvStr = System.getProperty("env", "dev");

        // opposite env
        String otherEnv = currentEnvStr.equals("dev") ? "prod" : "dev";

        UserService currentService = UserService.get(currentEnvStr);
        UserService otherService = UserService.get(otherEnv);

        CreateUserRequest user = TestUserFactory.defaultUser("env@mail.com", 30);

        // Create in current env
        currentService.createUser(user);

        // Verify NOT visible in other env
        Result<User> result = otherService.getUser(user.getEmail());

        UserValidator.validateError(result, 404, "User not found", softly);

        softly.assertAll();
    }
}
