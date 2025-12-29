package com.fusion.example.feature.lab.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemLabHeaderM3Binding;
import com.fusion.example.feature.lab.model.LabHeader;

public class LabHeaderDelegate extends JavaDelegate<LabHeader, ItemLabHeaderM3Binding> {

    @Nullable
    @Override
    public Object getUniqueKey(@NonNull LabHeader item) {
        return item.getTitle(); // Title as ID for headers
    }

    @NonNull
    @Override
    protected ItemLabHeaderM3Binding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemLabHeaderM3Binding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemLabHeaderM3Binding binding, @NonNull LabHeader item) {
        binding.tvTitle.setText(item.getTitle());
        if (item.getAction() != null) {
            binding.tvAction.setText(item.getAction());
            binding.tvAction.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.tvAction.setVisibility(android.view.View.GONE);
        }
        
        binding.tvAction.setOnClickListener(v -> 
            Toast.makeText(v.getContext(), "Clicked: " + item.getAction(), Toast.LENGTH_SHORT).show()
        );
    }
}
