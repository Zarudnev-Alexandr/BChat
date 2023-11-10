package com.example.bchatmobile;

public class MessageObj {
    private String text;
    private String date;

    public MessageObj(String text, String date) {
        this.text = text;
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }
}

