package com.fusion.example.feature.lab.model;

import androidx.annotation.DrawableRes;

public class LabApp {
    private final String id;
    private final String title;
    private final String description;
    private final float rating;
    @DrawableRes
    private final int iconRes;
    private final boolean isPromoted;

    public LabApp(String id, String title, String description, float rating, int iconRes, boolean isPromoted) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rating = rating;
        this.iconRes = iconRes;
        this.isPromoted = isPromoted;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public float getRating() {
        return rating;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean isPromoted() {
        return isPromoted;
    }
}
