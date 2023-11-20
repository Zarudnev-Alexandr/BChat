package com.example.bchatmobile;

public class BootcampApplication {

    private String text;
    private String role;
    private int id;
    private int bootcamp_id;
    private int user_id;

    public BootcampApplication(String text, String role, int id, int bootcamp_id, int user_id) {
        this.text = text;
        this.role = role;
        this.id = id;
        this.bootcamp_id = bootcamp_id;
        this.user_id = user_id;
    }

    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public int getId() {
        return id;
    }

    public int getBootcampId() {
        return bootcamp_id;
    }

    public int getUserId() {
        return user_id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBootcampId(int bootcamp_id) {
        this.bootcamp_id = bootcamp_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }
}
