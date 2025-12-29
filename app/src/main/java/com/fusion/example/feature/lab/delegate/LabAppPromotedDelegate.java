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
    public Object getUniqueKey(@NonNull LabApp item) {
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
        binding.ivIcon.setImageResource(item.getIconRes());
        
        // Load network image using Coil via Kotlin Helper
        // Use a stable seed based on ID to ensure the image doesn't change on scroll
        String url = "https://picsum.photos/seed/" + item.getId() + "/800/400";
        binding.ivCover.clearColorFilter(); // Clear XML tint
        CoilHelperKt.loadUrl(binding.ivCover, url, false);
        
        binding.getRoot().setOnClickListener(v ->
            Toast.makeText(v.getContext(), "Opening " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }
}
