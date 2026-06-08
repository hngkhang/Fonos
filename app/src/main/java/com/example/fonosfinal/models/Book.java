package com.example.fonosfinal.models;

public class Book {

    private final String title;
    private final String author;
    private final String duration;
    private final String rating;
    private final int coverType;
    private final String category;
    private final String narrator;

    public Book(String title, String author, String price) {
        this(title, author, price, "4.8", 0, "Audiobook");
    }

    public Book(String title, String author, String duration, String rating, int coverType, String category) {
        this(title, author, duration, rating, coverType, category, "Narrated by Fonos Studio");
    }

    public Book(String title, String author, String duration, String rating, int coverType, String category, String narrator) {
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.rating = rating;
        this.coverType = coverType;
        this.category = category;
        this.narrator = narrator;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDuration() {
        return duration;
    }

    public String getRating() {
        return rating;
    }

    public int getCoverType() {
        return coverType;
    }

    public String getCategory() {
        return category;
    }

    public String getNarrator() {
        return narrator;
    }
}
