package com.ben.sdet.base;

import org.testng.annotations.BeforeClass;

public abstract class BaseTest {

    @BeforeClass
    public void initialize() {
        // Shared test setup will be added in later phases
    }
}
