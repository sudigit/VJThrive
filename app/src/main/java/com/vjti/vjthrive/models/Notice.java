package com.vjti.vjthrive.models;

import java.util.List;

public class Notice {
    private String id;
    private String author;
    private String title;
    private String content;
    private String attachment;
    private long timestamp;
    private List<String> target_programme;
    private List<String> target_dept;
    private List<String> target_branch;
    private List<String> target_year;

    // Required empty constructor for Firestore
    public Notice() {
    }

    public Notice(String id, String author, String title, String content, String attachment, long timestamp, List<String> target_programme, List<String> target_dept, List<String> target_branch, List<String> target_year) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.content = content;
        this.attachment = attachment;
        this.timestamp = timestamp;
        this.target_programme = target_programme;
        this.target_dept = target_dept;
        this.target_branch = target_branch;
        this.target_year = target_year;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getTarget_programme() {
        return target_programme;
    }

    public void setTarget_programme(List<String> target_programme) {
        this.target_programme = target_programme;
    }

    public List<String> getTarget_dept() {
        return target_dept;
    }

    public void setTarget_dept(List<String> target_dept) {
        this.target_dept = target_dept;
    }

    public List<String> getTarget_branch() {
        return target_branch;
    }

    public void setTarget_branch(List<String> target_branch) {
        this.target_branch = target_branch;
    }

    public List<String> getTarget_year() {
        return target_year;
    }

    public void setTarget_year(List<String> target_year) {
        this.target_year = target_year;
    }
}
