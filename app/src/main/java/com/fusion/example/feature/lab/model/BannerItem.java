package com.fusion.example.feature.lab.model;

public class BannerItem {
    private final String title;
    private final String imageUrl;

    public BannerItem(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
