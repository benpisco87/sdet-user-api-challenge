package com.ben.sdet.common;

public class Result<T> {

    private final int statusCode;
    private final T data;
    private final Object error;
    private final String rawBody;

    public Result(int statusCode, T data, Object error, String rawBody) {
        this.statusCode = statusCode;
        this.data = data;
        this.error = error;
        this.rawBody = rawBody;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getData() {
        return data;
    }

    public <E> E getError(Class<E> clazz) {
        return clazz.cast(error);
    }

    public String getRawBody() {
        return rawBody;
    }
}
