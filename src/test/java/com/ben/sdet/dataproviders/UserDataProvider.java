package com.ben.sdet.dataproviders;

import org.testng.annotations.DataProvider;

import com.ben.sdet.dto.UserTestData;

import utils.YamlLoader;

import java.util.List;

public final class UserDataProvider {

    private static final String FILE = "testdata/users.yaml";

    private UserDataProvider() {}

    @DataProvider(name = "validUsers")
    public static Object[][] validUsers() {
        List<UserTestData> data = YamlLoader.load(FILE, "validUsers", UserTestData.class);
        return data.stream()
                .map(d -> new Object[]{d.getUser(), d.getLabel()})
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "invalidUsers")
    public static Object[][] invalidUsers() {
        List<UserTestData> data = YamlLoader.load(FILE, "invalidUsers", UserTestData.class);
        return data.stream()
                .map(d -> new Object[]{d.getUser(), d.getLabel()})
                .toArray(Object[][]::new);
    }
}
