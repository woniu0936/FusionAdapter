package com.fusion.example.feature.lab.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemLabAppRowM3Binding;
import com.fusion.example.feature.lab.model.LabApp;
import com.fusion.example.utils.CoilHelperKt;

public class LabAppStandardDelegate extends JavaDelegate<LabApp, ItemLabAppRowM3Binding> {

    @Nullable
    @Override
    public Object getStableId(@NonNull LabApp item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemLabAppRowM3Binding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemLabAppRowM3Binding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemLabAppRowM3Binding binding, @NonNull LabApp item) {
        binding.tvTitle.setText(item.getTitle());
        binding.tvDesc.setText(String.format("%.1f • %s", item.getRating(), item.getDescription()));
        
        // Load network icon
        String url = "https://picsum.photos/seed/" + item.getId() + "/200/200";
        CoilHelperKt.loadUrl(binding.ivIcon, url, true); // true for circle crop
        
        binding.getRoot().setOnClickListener(v ->
            Toast.makeText(v.getContext(), "Opening " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }
}
