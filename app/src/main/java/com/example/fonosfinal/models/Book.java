package com.example.fonosfinal.models;

public class Book {

    private String remoteId;
    private String slug;
    private String title;
    private String author;
    private String duration;
    private String rating;
    private int coverType;
    private String category;
    private String narrator;
    private String searchTitle;

    private String description;
    private String coverUrl;
    private long listenCount;
    private String createdAt;
    private String importedAt;

    public Book() {
    }

    public Book(String title, String author, String price) {
        this(title, author, price, "4.8", 0, "Audiobook");
    }

    public Book(String title, String author, String duration, String rating, int coverType, String category) {
        this(title, author, duration, rating, coverType, category, "Unknown narrator");
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

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title == null || title.trim().isEmpty() ? "Untitled audiobook" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author == null ? "Unknown author" : author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDuration() {
        return duration == null ? "Unknown" : duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRating() {
        return rating == null || rating.trim().isEmpty() ? "No rating" : rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getCoverType() {
        return coverType;
    }

    public void setCoverType(int coverType) {
        this.coverType = coverType;
    }

    public String getCategory() {
        return category == null ? "Audiobook" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNarrator() {
        return narrator == null || narrator.trim().isEmpty() ? "Unknown narrator" : narrator;
    }

    public void setNarrator(String narrator) {
        this.narrator = narrator;
    }

    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public long getListenCount() {
        return listenCount;
    }

    public void setListenCount(long listenCount) {
        this.listenCount = listenCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(String importedAt) {
        this.importedAt = importedAt;
    }
}
