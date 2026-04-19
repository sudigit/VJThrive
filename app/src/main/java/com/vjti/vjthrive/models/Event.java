package com.vjti.vjthrive.models;

import com.google.firebase.Timestamp;

public class Event {
    private String title;
    private String description;
    private Timestamp eventDate;
    private String clubId;
    private String createdBy;
    private String attachment;

    public Event() {
        // Required empty constructor for Firestore
    }

    public Event(String title, String description, Timestamp eventDate, String clubId, String createdBy, String attachment) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.clubId = clubId;
        this.createdBy = createdBy;
        this.attachment = attachment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    // Helper to get milliseconds
    public long getEventDateMillis() {
        return eventDate != null ? eventDate.toDate().getTime() : 0;
    }
}
