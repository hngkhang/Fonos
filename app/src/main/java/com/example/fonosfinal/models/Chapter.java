package com.example.fonosfinal.models;

import java.io.Serializable;

public class Chapter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String bookId;
    private final String chapterId;
    private final String number;
    private final String title;
    private final String duration;
    private final String status;
    private final int chapterIndex;
    private final String audioUrl;
    private final String localPath;

    public Chapter(String number, String title, String duration, String status,
                   int chapterIndex, String audioUrl) {
        this(null, null, number, title, duration, status, chapterIndex, audioUrl, null);
    }

    public Chapter(String bookId, String chapterId, String number, String title,
                   String duration, String status, int chapterIndex,
                   String audioUrl, String localPath) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        this.number = number;
        this.title = title;
        this.duration = duration;
        this.status = status;
        this.chapterIndex = chapterIndex;
        this.audioUrl = audioUrl;
        this.localPath = localPath;
    }

    public String getBookId() {
        return bookId;
    }

    public String getChapterId() {
        return chapterId;
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

    public String getLocalPath() {
        return localPath;
    }

    public String getPlaybackUrl() {
        if (localPath != null && !localPath.trim().isEmpty()) {
            return localPath;
        }
        return audioUrl;
    }
}
