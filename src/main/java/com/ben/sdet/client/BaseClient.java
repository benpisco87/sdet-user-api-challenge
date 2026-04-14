package com.ben.sdet.client;

import com.ben.sdet.config.ServiceConfig;
import com.ben.sdet.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ben.sdet.factory.HttpClientFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class BaseClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    protected <T, E> Result<T> execute(Request request, Class<T> successClass, Class<E> errorClass) {
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            T data = null;
            E error = null;

            if (!body.isEmpty()) {
                try {
                    if (response.isSuccessful()) {
                        data = MAPPER.readValue(body, successClass);
                    } else {
                        error = MAPPER.readValue(body, errorClass);
                    }
                } catch (Exception ignored) {
                    // keep rawBody for debugging
                }
            }

            return new Result<>(response.code(), data, error, body);
        } catch (Exception e) {
            throw new RuntimeException("API call failed", e);
        }
    }
}
