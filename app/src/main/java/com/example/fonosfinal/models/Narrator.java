package com.example.fonosfinal.models;

public class Narrator {

    private final String name;
    private final String subtitle;
    private final String rating;

    public Narrator(String name, String subtitle, String rating) {
        this.name = name;
        this.subtitle = subtitle;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getRating() {
        return rating;
    }
}
