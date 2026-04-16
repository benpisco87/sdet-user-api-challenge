package com.ben.sdet.client;

import java.net.ConnectException;
import java.util.List;

import org.testng.SkipException;

import com.ben.sdet.common.Result;
import com.ben.sdet.config.ServiceConfig;
import com.ben.sdet.utils.ObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ben.sdet.factory.HttpClientFactory;
import com.ben.sdet.logging.LoggerUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class BaseClient {

    protected static final ObjectMapper MAPPER = ObjectMapperProvider.get();

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

    protected Request.Builder baseJsonRequest(String path) {    
        return baseRequest(path).addHeader("Content-Type", "application/json");
    }

    // =========================
    // SINGLE OBJECT
    // =========================
    protected <T, E> Result<T> execute(Request request,
                                   Class<T> successClass,
                                   Class<E> errorClass) {
        try (Response response = client.newCall(request).execute()) {

            String body = response.body() != null ? response.body().string() : "";

            T data = null;
            E error = null;

            if (response.isSuccessful()) {
                try {
                    if (successClass != Void.class && !body.isEmpty()) {
                        data = MAPPER.readValue(body, successClass);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to parse SUCCESS response. Status: "
                                    + response.code() + ", Body: " + body,
                            e
                    );
                }
            } else {
                try {
                    if (!body.isEmpty() && errorClass != null) {
                        error = MAPPER.readValue(body, errorClass);
                    }
                } catch (Exception e) {
                    LoggerUtils.debug(String.format(
                            "Failed to parse error response: %s Exception: %s",
                            body, e.getMessage()
                    ));
                }
            }

            return new Result<>(response.code(), data, error, body);

        } catch (Exception e) {

            if (isConnectionError(e)) {
                String message = String.format(
                        "API not reachable at %s → skipping test",
                        request.url()
                );

                LoggerUtils.error(message, e);

                throw new SkipException(message);
            }

            throw new RuntimeException("API call failed", e);
        }
    }

    // =========================
    // LIST SUPPORT
    // =========================
    protected <T, E> Result<List<T>> executeList(Request request,
                                                Class<T> successClass,
                                                Class<E> errorClass) {

        try (Response response = client.newCall(request).execute()) {

            String body = response.body() != null ? response.body().string() : "";

            List<T> data = null;
            E error = null;

            if (response.isSuccessful()) {
                try {
                    if (!body.isEmpty()) {
                        data = MAPPER.readValue(
                                body,
                                MAPPER.getTypeFactory()
                                        .constructCollectionType(List.class, successClass)
                        );
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to parse LIST response. Status: "
                                    + response.code() + ", Body: " + body,
                            e
                    );
                }
            } else {
                try {
                    if (!body.isEmpty() && errorClass != null) {
                        error = MAPPER.readValue(body, errorClass);
                    }
                } catch (Exception e) {
                    // ignore, rawBody is enough
                }
            }

            return new Result<>(response.code(), data, error, body);

        } catch (Exception e) {
            throw new RuntimeException("API call failed", e);
        }
    }

    private boolean isConnectionError(Throwable e) {
        if (e instanceof ConnectException) return true;

        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof ConnectException) return true;
            cause = cause.getCause();
        }

        return false;
    }
}
