package com.fusion.example.feature.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemMsgImageBinding;
import com.fusion.example.model.FusionMessage;
import com.fusion.example.utils.ChatStyleHelper;

public class JavaMsgImageDelegate extends JavaDelegate<FusionMessage, ItemMsgImageBinding> {

    @Override
    @Nullable
    public Object getUniqueKey(@NonNull FusionMessage item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemMsgImageBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemMsgImageBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemMsgImageBinding binding, @NonNull FusionMessage item) {
        binding.ivImage.setContentDescription(item.getContent());
        ChatStyleHelper.INSTANCE.bindImageMsg(binding, item.isMe());
    }
}