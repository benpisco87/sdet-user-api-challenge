package com.ben.sdet.config;

public final class ConfigProvider {

    private ConfigProvider() {
        // Utility class
    }

    private static Environment getEnvironment() {
        return Environment.from(System.getProperty("env", "dev"));
    }

    public static <T extends ServiceConfig> T get(Class<T> clazz) {
        try {
            return clazz
                    .getDeclaredConstructor(Environment.class)
                    .newInstance(getEnvironment());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config for " + clazz.getSimpleName(), e);
        }
    }
}
