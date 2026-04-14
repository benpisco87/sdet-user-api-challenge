package com.ben.sdet.config;

public abstract class BaseServiceConfig implements ServiceConfig {

    protected final Environment environment;

    protected BaseServiceConfig(Environment environment) {
        this.environment = environment;
    }

    protected String resolveBaseUrl() {
        return switch (environment) {
            case DEV -> "http://localhost:3000/dev";
            case PROD -> "http://localhost:3000/prod";
        };
    }
}
