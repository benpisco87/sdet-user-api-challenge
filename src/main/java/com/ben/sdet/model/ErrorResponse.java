package com.ben.sdet.model;

public class ErrorResponse {

    private String error;

    public ErrorResponse() {
        // Jackson needs a no-argument constructor
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
