package com.example.fonosfinal.models;

public class ListeningProgress {

    private final Book book;
    private final Chapter chapter;
    private final int positionMs;
    private final int durationMs;

    public ListeningProgress(Book book, Chapter chapter, int positionMs, int durationMs) {
        this.book = book;
        this.chapter = chapter;
        this.positionMs = positionMs;
        this.durationMs = durationMs;
    }

    public Book getBook() {
        return book;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public int getPositionMs() {
        return positionMs;
    }

    public int getDurationMs() {
        return durationMs;
    }
}
