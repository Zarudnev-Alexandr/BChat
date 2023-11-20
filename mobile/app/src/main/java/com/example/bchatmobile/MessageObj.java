package com.example.bchatmobile;

public class MessageObj {
    private String text;

    private String sender;
    private Integer id;
    private Integer sender_id;

    public MessageObj(Integer id, String text, Integer sender_id, String sender) {
        this.id = id;
        this.text = text;
        this.sender_id = sender_id;
        this.sender = sender ;
    }

    public String getText() {
        return text;
    }
    public Integer getId() {
        return id;
    }

    public Integer getSender_id() {
        return sender_id;
    }
    public String getSender() {
        return sender;
    }
}

