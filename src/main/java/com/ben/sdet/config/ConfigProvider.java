package com.ben.sdet.config;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigProvider {

    private static final Properties PROPS = new Properties();

    static {
        try {
            String env = System.getProperty("env", "dev");
            String fileName = "config/" + env + ".properties";

            InputStream is = ConfigProvider.class
                    .getClassLoader()
                    .getResourceAsStream(fileName);

            if (is == null) {
                throw new RuntimeException("Config file not found: " + fileName);
            }

            PROPS.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    private ConfigProvider() {}

    public static String get(String key) {
        return PROPS.getProperty(key);
    }
}
