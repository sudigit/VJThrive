package com.vjti.vjthrive.models;

public class Message {
    private String text;
    private String senderId;
    private Object timestamp;

    public Message() {
        // Required empty constructor for Firestore
    }

    public Message(String text, String senderId, Object timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
