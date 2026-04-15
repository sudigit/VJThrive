package com.vjti.vjthrive.models;

public class ClubEvent {
    private String id;
    private String club;
    private String title;
    private String content;
    private String attachment;
    private String colour;
    private long eventDate;
    private long timestamp;

    // Required empty constructor for Firestore
    public ClubEvent() {
    }

    public ClubEvent(String id, String club, String title, String content, String attachment, String colour, long eventDate, long timestamp) {
        this.id = id;
        this.club = club;
        this.title = title;
        this.content = content;
        this.attachment = attachment;
        this.colour = colour;
        this.eventDate = eventDate;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClub() {
        return club;
    }

    public void setClub(String club) {
        this.club = club;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
