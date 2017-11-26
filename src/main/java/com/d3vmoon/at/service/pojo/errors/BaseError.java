package com.d3vmoon.at.service.pojo.errors;

public class BaseError {

    static final String HEROKU_URL = System.getenv("HEROKU_URL");

    final String login = HEROKU_URL + "/p/login.html";

    public final String message;
    public final int status;

    public BaseError(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
