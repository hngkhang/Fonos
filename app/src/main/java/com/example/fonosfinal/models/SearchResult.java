package com.example.fonosfinal.models;

public class SearchResult {

    private final String title;
    private final String author;
    private final String narrator;
    private final String duration;
    private final String rating;
    private final String category;
    private final int coverType;

    public SearchResult(String title, String author, String narrator, String duration, String rating, String category, int coverType) {
        this.title = title;
        this.author = author;
        this.narrator = narrator;
        this.duration = duration;
        this.rating = rating;
        this.category = category;
        this.coverType = coverType;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getNarrator() {
        return narrator;
    }

    public String getDuration() {
        return duration;
    }

    public String getRating() {
        return rating;
    }

    public String getCategory() {
        return category;
    }

    public int getCoverType() {
        return coverType;
    }
}
