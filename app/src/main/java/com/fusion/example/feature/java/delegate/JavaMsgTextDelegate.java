package com.fusion.example.feature.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemMsgTextBinding;
import com.fusion.example.model.FusionMessage;
import com.fusion.example.utils.ChatStyleHelper;

public class JavaMsgTextDelegate extends JavaDelegate<FusionMessage, ItemMsgTextBinding> {

    @Override
    @Nullable
    public Object getUniqueKey(@NonNull FusionMessage item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemMsgTextBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemMsgTextBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemMsgTextBinding binding, @NonNull FusionMessage item) {
        binding.tvContent.setText(item.getContent());
        ChatStyleHelper.INSTANCE.bindTextMsg(binding, item.isMe());
    }
}
