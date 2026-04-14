package com.ben.sdet.logging;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        log.info("➡️ Request: {} {}", request.method(), request.url());

        Response response = chain.proceed(request);

        log.info("⬅️ Response: {} {}", response.code(), response.request().url());

        return response;
    }
}
