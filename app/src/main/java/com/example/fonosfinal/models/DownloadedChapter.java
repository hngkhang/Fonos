package com.example.fonosfinal.models;

public class DownloadedChapter {

    private final String userId;
    private final String bookRemoteId;
    private final String chapterId;
    private final int chapterIndex;
    private final String chapterNumber;
    private final String title;
    private final String duration;
    private final String audioUrl;
    private final String localPath;
    private final long fileSizeBytes;
    private final String status;

    public DownloadedChapter(String userId, String bookRemoteId, String chapterId,
                             int chapterIndex, String chapterNumber, String title,
                             String duration, String audioUrl, String localPath,
                             long fileSizeBytes, String status) {
        this.userId = userId;
        this.bookRemoteId = bookRemoteId;
        this.chapterId = chapterId;
        this.chapterIndex = chapterIndex;
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.duration = duration;
        this.audioUrl = audioUrl;
        this.localPath = localPath;
        this.fileSizeBytes = fileSizeBytes;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getBookRemoteId() {
        return bookRemoteId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public String getChapterNumber() {
        return chapterNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getStatus() {
        return status;
    }
}
