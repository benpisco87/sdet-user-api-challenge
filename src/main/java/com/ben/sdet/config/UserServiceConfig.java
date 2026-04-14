package com.ben.sdet.config;

public class UserServiceConfig extends BaseServiceConfig {

    public UserServiceConfig(Environment environment) {
        super(environment);
    }

    @Override
    public String getBaseUrl() {
        return resolveBaseUrl();
    }
}
