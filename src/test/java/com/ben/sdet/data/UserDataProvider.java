package com.ben.sdet.data;

import org.testng.annotations.DataProvider;

import com.ben.sdet.dto.UserTestData;
import com.ben.sdet.dto.user.CreateUserRequest;
import com.ben.sdet.utils.YamlLoader;
import com.ben.sdet.utils.data.TestDataUtils;

import java.util.List;

public final class UserDataProvider {

    private static final String FILE = "testdata/users.yaml";

    private static List<UserTestData> validUsers;
    private static List<UserTestData> invalidUsers;

    private UserDataProvider() {}

    // =========================
    // Lazy load (thread-safe enough for TestNG)
    // =========================
    private static synchronized void load() {
        if (validUsers == null) {
            validUsers = YamlLoader.load(FILE, "validUsers", UserTestData.class);
            invalidUsers = YamlLoader.load(FILE, "invalidUsers", UserTestData.class);
        }
    }

    // =========================
    // Helpers
    // =========================
    private static CreateUserRequest cloneWithUniqueEmail(CreateUserRequest base) {
        return base.toBuilder()
                .email(TestDataUtils.uniqueEmail(base.getEmail()))
                .build();
    }

    // =========================
    // DataProviders
    // =========================

    @DataProvider(name = "validUsers", parallel = true)
    public static Object[][] validUsers() {
        load();

        return validUsers.stream()
                .map(d -> new Object[]{
                        cloneWithUniqueEmail(d.getRequest()),
                        d.getLabel()
                })
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "invalidUsers", parallel = true)
    public static Object[][] invalidUsers() {
        load();

        return invalidUsers.stream()
                .map(d -> new Object[]{
                        cloneWithUniqueEmail(d.getRequest()),
                        d.getLabel(),
                        d.getExpectedStatus(),
                        d.getExpectedError()
                })
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "invalidTokens", parallel = true)
    public static Object[][] invalidTokens() {
        return new Object[][] {
                {"", "Empty token"},
                {"invalid-token-123", "Random invalid token"},
                {"1234567890abc", "Token with correct format but invalid value"},
                {"MYSECRETTOKEN", "Valid token but wrong case"},
        };
    }
}
