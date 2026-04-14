package com.ben.sdet.dataproviders;

import org.testng.annotations.DataProvider;

public final class YamlDataProvider {

    private YamlDataProvider() {
        // Utility class for TestNG data providers
    }

    @DataProvider(name = "yamlData")
    public static Object[][] yamlData() {
        return new Object[][] {
                {"sample"}
        };
    }
}
