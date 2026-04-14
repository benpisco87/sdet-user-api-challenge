package com.ben.sdet.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ben.sdet.logging.LoggerUtils;

public class DemoTest {

    @Test
    public void demoTest() {
        Assert.assertTrue(true, "This test should always pass");
        LoggerUtils.info("This is a demo test log message.");
    }

}
