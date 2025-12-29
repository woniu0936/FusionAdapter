package com.fusion.example.feature.lab.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemLabBannerM3Binding;
import com.fusion.example.feature.lab.model.LabBanner;

public class LabBannerDelegate extends JavaDelegate<LabBanner, ItemLabBannerM3Binding> {

    @Nullable
    @Override
    public Object getUniqueKey(@NonNull LabBanner item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemLabBannerM3Binding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemLabBannerM3Binding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemLabBannerM3Binding binding, @NonNull LabBanner item) {
        binding.tvTitle.setText(item.getTitle());
        binding.ivImage.setImageResource(item.getImageRes());
        
        binding.getRoot().setOnClickListener(v ->
            Toast.makeText(v.getContext(), "Clicked Banner: " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }
}
