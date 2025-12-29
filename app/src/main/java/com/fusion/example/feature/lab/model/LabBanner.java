package com.fusion.example.feature.lab.model;

import androidx.annotation.DrawableRes;

public class LabBanner {
    private final String id;
    private final String title;
    @DrawableRes
    private final int imageRes;

    public LabBanner(String id, String title, int imageRes) {
        this.id = id;
        this.title = title;
        this.imageRes = imageRes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getImageRes() {
        return imageRes;
    }
}
