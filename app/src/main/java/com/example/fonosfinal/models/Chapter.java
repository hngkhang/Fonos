package com.example.fonosfinal.models;

public class Chapter {

    private final String number;
    private final String title;
    private final String duration;
    private final String status;

    public Chapter(String number, String title, String duration, String status) {
        this.number = number;
        this.title = title;
        this.duration = duration;
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getStatus() {
        return status;
    }
}
