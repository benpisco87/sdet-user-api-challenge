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
        String requestBody = "";

        if (request.body() != null) {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            requestBody = buffer.readUtf8();
        }

        LoggerUtils.info("➡️ " + request.method() + " " + request.url());

        if (!requestBody.isBlank()) {
            String prettyRequest = JsonUtil.pretty(requestBody);
            LoggerUtils.attach("Request Body", prettyRequest);
        }

        Response response = chain.proceed(request);

        ResponseBody responseBody = response.body();
        String bodyString = responseBody != null ? responseBody.string() : "";

        LoggerUtils.info("⬅️ " + response.code() + " " + request.url());

        if (!bodyString.isBlank()) {
            String prettyResponse = JsonUtil.pretty(bodyString);
            LoggerUtils.attach("Response Body", prettyResponse);
        }

        return response.newBuilder()
                .body(ResponseBody.create(bodyString, responseBody != null ? responseBody.contentType() : null))
                .build();
    }
}
