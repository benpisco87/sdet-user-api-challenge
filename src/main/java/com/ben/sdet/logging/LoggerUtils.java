package com.ben.sdet.logging;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LoggerUtils {

    private LoggerUtils() {
        // Utility class
    }

    public static void info(String message) {
        log.info(message);
        Allure.step(message);
    }

    public static void debug(String message) {
        log.debug(message);
        Allure.step(message);
    }

    public static void error(String message, Throwable throwable) {
        log.error(message, throwable);
        Allure.step("Error occurred: " + message);
    }

    public static void attach(String name, String content) {
        Allure.addAttachment(name, content);
    }
}
