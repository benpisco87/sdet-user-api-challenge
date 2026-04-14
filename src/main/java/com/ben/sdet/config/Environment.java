package com.ben.sdet.config;

public enum Environment {
    DEV, PROD;

    public static Environment from(String value) {
        if (value == null || value.isBlank()) {
            return DEV;
        }
        try {
            return Environment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported environment: " + value, ex);
        }
    }
}
