package com.example.nos.secureflomessaging;
import android.app.Application;

import com.example.nos.secureflomessaging.data.User;
/**
 * Created by Nos on 11/26/2016.
 */

public class RESTServiceApplication extends Application {
    private static RESTServiceApplication instance;
    private User user;
    private String accessToken;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        user = new User();
    }

    public static RESTServiceApplication getInstance() {
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}

