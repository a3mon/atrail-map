package com.d3vmoon.at.service.pojo;

public class Credentials {

    public final String email;
    public final String password;
    public final String googleToken;

    public Credentials(String email, String password, String googleToken) {
        this.email = email;
        this.password = password;
        this.googleToken = googleToken;
    }

    public boolean isGoogleRequst() {
        return googleToken != null;
    }

    public boolean isEmailRequest() {
        return  password != null;
    }
}
