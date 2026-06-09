package com.example.fonosfinal.models;

public class Chapter {

    private final String number;
    private final String title;
    private final String duration;
    private final String status;
    private final int chapterIndex;
    private final String audioUrl;

    public Chapter(String number, String title, String duration, String status,
                   int chapterIndex, String audioUrl) {
        this.number = number;
        this.title = title;
        this.duration = duration;
        this.status = status;
        this.chapterIndex = chapterIndex;
        this.audioUrl = audioUrl;
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

    public int getChapterIndex() {
        return chapterIndex;
    }

    public String getAudioUrl() {
        return audioUrl;
    }
}
