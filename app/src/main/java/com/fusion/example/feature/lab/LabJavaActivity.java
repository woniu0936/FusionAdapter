package com.fusion.example.feature.lab;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fusion.adapter.FusionListAdapter;
import com.fusion.adapter.internal.registry.TypeDispatcher;
import com.fusion.example.R;
import com.fusion.example.databinding.ActivityBaseFixedBinding;
import com.fusion.example.feature.lab.delegate.LabAppPromotedDelegate;
import com.fusion.example.feature.lab.delegate.LabAppStandardDelegate;
import com.fusion.example.feature.lab.delegate.LabBannerDelegate;
import com.fusion.example.feature.lab.delegate.LabHeaderDelegate;
import com.fusion.example.feature.lab.model.BannerItem;
import com.fusion.example.feature.lab.model.LabApp;
import com.fusion.example.feature.lab.model.LabBanner;
import com.fusion.example.feature.lab.model.LabHeader;
import com.fusion.example.utils.ExtensionsKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabJavaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBaseFixedBinding b = ActivityBaseFixedBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        ExtensionsKt.fullStatusBar(this, b.getRoot());
        b.toolbar.setTitle("Fusion Store (Java)");

        FusionListAdapter adapter = new FusionListAdapter();

        // 1. [1-to-1 Mapping] Header -> LabHeaderDelegate
        adapter.register(LabHeader.class, new LabHeaderDelegate());

        // 2. [1-to-1 Mapping] Banner -> LabBannerDelegate
        adapter.register(LabBanner.class, new LabBannerDelegate());

        // 3. [1-to-Many Mapping] LabApp -> Standard or Promoted Delegate
        // Demonstrates mapping one data type to multiple view types based on properties
        TypeDispatcher<LabApp> appDispatcher = new TypeDispatcher.Builder<LabApp>()
                .uniqueKey(LabApp::getId)
                .viewType(app -> app.isPromoted() ? 2 : 1) // 2: Promoted, 1: Standard
                .delegate(1, new LabAppStandardDelegate())
                .delegate(2, new LabAppPromotedDelegate())
                .build();
        adapter.registerDispatcher(LabApp.class, appDispatcher);

        b.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerView.setAdapter(adapter);

        // Populate Data
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Object> items = new ArrayList<>();

            // 1. Top Featured Banners (Carousel)
            List<BannerItem> topBanners = Arrays.asList(
                new BannerItem("Summer Sale: Up to 50% Off", "https://picsum.photos/seed/banner1/800/400"),
                new BannerItem("New Collection Release", "https://picsum.photos/seed/banner2/800/400"),
                new BannerItem("Editor's Top Picks", "https://picsum.photos/seed/banner3/800/400")
            );
            items.add(new LabBanner("b1", topBanners));

            // 2. Recommended Section (Standard Rows)
            items.add(new LabHeader("Recommended for you", "View More"));
            items.add(new LabApp("a1", "Fusion Chat", "Social • 42MB", 4.8f, R.drawable.ic_chat, false));
            items.add(new LabApp("a2", "Visual Lab Pro", "Photography • 156MB", 4.5f, R.drawable.ic_visuals, false));
            items.add(new LabApp("a3", "Java Masterclass", "Education • 12MB", 4.9f, R.drawable.ic_java, false));

            // 3. Featured Section (Promoted Cards)
            items.add(new LabHeader("Editor's Choice", null));
            items.add(new LabApp("p1", "Ultimate Marketplace", "The best marketplace for digital goods. Shop now!", 5.0f, R.drawable.ic_market, true));
            
            // Middle Banner
            List<BannerItem> midBanners = Arrays.asList(
                new BannerItem("Pro Tools Bundle", "https://picsum.photos/seed/banner4/800/400"),
                new BannerItem("Creative Cloud", "https://picsum.photos/seed/banner5/800/400")
            );
            items.add(new LabBanner("b2", midBanners));
            
            items.add(new LabApp("p2", "Moments Pro", "Share your life in style with premium filters.", 4.7f, R.drawable.ic_moments, true));

            // 4. Trending Apps (Bulk Data)
            items.add(new LabHeader("Trending this week", "See all"));
            for (int i = 1; i <= 10; i++) {
                float rating = 3.5f + (i % 15) / 10f;
                items.add(new LabApp("trend_" + i, "Super Utility " + i, "Tools • " + (i * 2) + "MB", rating, R.drawable.ic_lab, false));
            }

            // 5. Another Promoted Card in the middle
            items.add(new LabHeader("Special Offer", "Get Coupon"));
            items.add(new LabApp("p3", "Discovery Explorer", "Explore hidden gems around the world with our new offline maps feature.", 4.6f, R.drawable.ic_discovery, true));

            // 6. More Categories
            items.add(new LabHeader("Recently Updated", "Manage"));
            for (int i = 1; i <= 5; i++) {
                items.add(new LabApp("upd_" + i, "App Update " + i, "Productivity • 2" + i + "MB", 4.2f, R.drawable.ic_visuals, false));
            }

            // 7. Footer Banner
            List<BannerItem> footerBanners = Arrays.asList(
                new BannerItem("Join our Developer Program", "https://picsum.photos/seed/banner6/800/400")
            );
            items.add(new LabBanner("b3", footerBanners));

            adapter.submitList(items, null);
        }, 500);
    }
}