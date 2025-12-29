package com.fusion.example.feature.lab.model;

import java.util.List;

public class LabBanner {
    private final String id;
    private final List<BannerItem> items;

    public LabBanner(String id, List<BannerItem> items) {
        this.id = id;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public List<BannerItem> getItems() {
        return items;
    }
}
