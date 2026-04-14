package com.ben.sdet.factory;

import com.ben.sdet.logging.LoggingInterceptor;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public final class HttpClientFactory {

    private static OkHttpClient client;

    private HttpClientFactory() {
        // Utility class for creating OkHttp clients
    }

    public static synchronized OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new LoggingInterceptor())
                    .build();
        }
        return client;
    }
}
