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
        attach(getTimestampWithLogLevel("INFO") + message, message);
    }

    public static void debug(String message) {
        log.debug(message);
        attach(getTimestampWithLogLevel("DEBUG") + message, message);
    }

    public static void error(String message, Throwable throwable) {
        log.error(message, throwable);
        attach(getTimestampWithLogLevel("ERROR") + message, message + "\n" + throwable.getMessage());
    }

    public static void attach(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    private static String getTimestampWithLogLevel(String level) {
        String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("[%s] [%s]", ts, level);
    }
}
