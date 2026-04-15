package com.ben.sdet.config;

public class UserServiceConfig implements ServiceConfig {

    @Override
    public String getBaseUrl() {
        return ConfigProvider.get("user.baseUrl");
    }
}
