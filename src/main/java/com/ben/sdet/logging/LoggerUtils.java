package com.ben.sdet.logging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LoggerUtils {

    private LoggerUtils() {
        // Utility class
    }

    public static void info(String message) {
        log.info(message);
    }

    public static void debug(String message) {
        log.debug(message);
    }

    public static void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }
}
