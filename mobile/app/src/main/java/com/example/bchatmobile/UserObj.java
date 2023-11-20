package com.example.bchatmobile;

public class UserObj {
    private int userId;
    private String username;

    public UserObj(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}

