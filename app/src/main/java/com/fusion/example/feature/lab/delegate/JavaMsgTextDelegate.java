package com.fusion.example.feature.lab.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemLabRecordBinding;
import com.fusion.example.core.model.ChatMessage;

public class JavaMsgTextDelegate extends JavaDelegate<ChatMessage, ItemLabRecordBinding> {
    @Override
    @Nullable
    public Object getUniqueKey(@NonNull ChatMessage item) { return item.getId(); }

    @NonNull
    @Override
    protected ItemLabRecordBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemLabRecordBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemLabRecordBinding binding, @NonNull ChatMessage item) {
        binding.tvTitle.setText(item.getContent());
        binding.tvId.setText("JAVA_ID: " + item.getId());
        binding.vStatusIndicatorBox.setCardBackgroundColor(0xFF005FB0); // Sapphire Blue
        binding.ivTypeIcon.setImageResource(android.R.drawable.ic_dialog_info);
    }
}