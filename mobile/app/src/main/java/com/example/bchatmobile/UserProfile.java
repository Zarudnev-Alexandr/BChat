package com.example.bchatmobile;

public class UserProfile{

    private String name;
    private String surname;
    private String birthDate;
    private String nickname;
    private String userAvatar;



    public UserProfile(String name, String surname, String birthDate, String nickname,String userAvatar) {
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.nickname = nickname;
        this.userAvatar = userAvatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}

