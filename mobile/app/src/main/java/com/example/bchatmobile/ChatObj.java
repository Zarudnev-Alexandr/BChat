package com.example.bchatmobile;

public class ChatObj{

    private int id;
    private String name;
    private String date;



    public ChatObj(int id, String name, String date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

}

