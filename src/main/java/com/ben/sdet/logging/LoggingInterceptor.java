package com.ben.sdet.logging;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // --- Read request body ---
        String requestBody = "";
        if (request.body() != null) {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            requestBody = buffer.readUtf8();
        }

        // --- Execute request ---
        Response response = chain.proceed(request);

        // --- Read response body ---
        ResponseBody responseBody = response.body();
        String responseBodyString = responseBody != null ? responseBody.string() : "";

        // --- Pretty print ---
        String prettyRequest = JsonUtil.pretty(requestBody);
        String prettyResponse = JsonUtil.pretty(responseBodyString);

        // --- Build single log ---
        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append("REQUEST:\n")
                .append(request.method()).append(" ").append(request.url()).append("\n\n");

        if (!requestBody.isBlank()) {
            logBuilder.append("Request Body:\n")
                    .append(prettyRequest).append("\n\n");
        }

        logBuilder.append("--------------------------------------------------\n");

        logBuilder.append("RESPONSE:\n")
                .append("Status: ").append(response.code()).append("\n\n");

        if (!responseBodyString.isBlank()) {
            logBuilder.append("Response Body:\n")
                    .append(prettyResponse).append("\n");
        }

        String finalLog = logBuilder.toString();

        // --- Console log ---
        log.info("\n{}", finalLog);

        // --- Allure attachment (SINGLE) ---
        String title = request.method() + " " + request.url() + " -> " + response.code();
        LoggerUtils.attach(title, finalLog);

        // --- Rebuild response body ---
        return response.newBuilder()
                .body(ResponseBody.create(
                        responseBodyString,
                        responseBody != null ? responseBody.contentType() : null
                ))
                .build();
    }
}
