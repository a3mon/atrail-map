package com.d3vmoon.at.service.pojo;

public class Confirmation {

    public final String email;
    public final String token;

    public Confirmation(String email, String token) {
        this.email = email;
        this.token = token;
    }
}