package com.example.bchatmobile;

public class BootcampMembers {

    private String role;
    private String nickname;
    private int id;
    private int application_id;

    public BootcampMembers(String role, String nickname, int id,int application_id) {
        this.role = role;
        this.nickname = nickname;
        this.id = id;
        this.application_id = application_id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return application_id;
    }

    public void setApplicationId(int application_id) {
        this.application_id = application_id;
    }

}
