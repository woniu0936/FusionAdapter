package com.fusion.example.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fusion.example.databinding.ItemMsgImageBinding;
import com.fusion.example.model.FusionMessage;
import com.fusion.adapter.delegate.JavaDelegate;

public class JavaMsgImageDelegate extends JavaDelegate<FusionMessage, ItemMsgImageBinding> {

    @NonNull
    @Override
    protected ItemMsgImageBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemMsgImageBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemMsgImageBinding binding, @NonNull FusionMessage item, int position) {
        binding.tvDesc.setText("Java Image: " + item.getId());
        binding.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
    }
}