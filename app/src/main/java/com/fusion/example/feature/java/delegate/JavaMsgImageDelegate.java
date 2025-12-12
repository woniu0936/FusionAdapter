package com.fusion.example.feature.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemMsgImageBinding;
import com.fusion.example.model.FusionMessage;
import com.fusion.example.utils.ChatStyleHelper;

public class JavaMsgImageDelegate extends JavaDelegate<FusionMessage, ItemMsgImageBinding> {

    @NonNull
    @Override
    protected ItemMsgImageBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemMsgImageBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemMsgImageBinding binding, @NonNull FusionMessage item, int position) {
        binding.ivImage.setContentDescription("Java Image: " + item.getId());
        ChatStyleHelper.INSTANCE.bindImageMsg(binding, item.isMe());
    }
}