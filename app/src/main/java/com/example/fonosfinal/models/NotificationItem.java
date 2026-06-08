package com.example.fonosfinal.models;

public class NotificationItem {

    private final String title;
    private final String description;
    private final String time;
    private final boolean isNew;
    private final int iconResId;

    public NotificationItem(String title, String description, String time, boolean isNew, int iconResId) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.isNew = isNew;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public boolean isNew() {
        return isNew;
    }

    public int getIconResId() {
        return iconResId;
    }
}
