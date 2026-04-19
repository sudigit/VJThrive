package com.vjti.vjthrive.models;

public class Message {
    private String text;
    private String senderId;
    private String senderName;
    private String attachment;
    private Object timestamp;

    public Message() {
        // Required empty constructor for Firestore
    }

    public Message(String text, String senderId, String senderName, String attachment, Object timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.attachment = attachment;
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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
