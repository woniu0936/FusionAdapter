package com.fusion.example.feature.lab.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemLabAppCardM3Binding;
import com.fusion.example.feature.lab.model.LabApp;
import com.fusion.example.utils.CoilHelperKt;

public class LabAppPromotedDelegate extends JavaDelegate<LabApp, ItemLabAppCardM3Binding> {

    @Nullable
    @Override
    public Object getStableId(@NonNull LabApp item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemLabAppCardM3Binding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemLabAppCardM3Binding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemLabAppCardM3Binding binding, @NonNull LabApp item) {
        binding.tvTitle.setText(item.getTitle());
        binding.tvDesc.setText(item.getDescription());
        binding.ratingBar.setRating(item.getRating());
        
        // Load network icon
        String iconUrl = "https://picsum.photos/seed/" + item.getId() + "_icon/200/200";
        CoilHelperKt.loadUrl(binding.ivIcon, iconUrl, true);
        
        // Load network cover image
        String url = "https://picsum.photos/seed/" + item.getId() + "/800/400";
        binding.ivCover.clearColorFilter(); // Clear XML tint
        CoilHelperKt.loadUrl(binding.ivCover, url, false);
        
        binding.getRoot().setOnClickListener(v ->
            Toast.makeText(v.getContext(), "Opening " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }
}
