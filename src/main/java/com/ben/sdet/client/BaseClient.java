package com.ben.sdet.client;

import com.ben.sdet.config.ServiceConfig;
import com.ben.sdet.factory.HttpClientFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public abstract class BaseClient {

    protected final ServiceConfig config;
    protected final OkHttpClient client;

    protected BaseClient(ServiceConfig config) {
        this.config = config;
        this.client = HttpClientFactory.getClient();
    }

    protected String url(String path) {
        return config.getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
    }

    protected Request.Builder baseRequest(String path) {
        return new Request.Builder().url(url(path));
    }
}
