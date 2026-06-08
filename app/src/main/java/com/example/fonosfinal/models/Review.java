package com.example.fonosfinal.models;

public class Review {

    private final String reviewerName;
    private final String rating;
    private final String body;
    private final String time;

    public Review(String reviewerName, String rating, String body, String time) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.body = body;
        this.time = time;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getRating() {
        return rating;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }
}
