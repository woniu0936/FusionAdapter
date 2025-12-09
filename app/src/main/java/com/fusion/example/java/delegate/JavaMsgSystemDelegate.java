package com.fusion.example.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fusion.example.databinding.ItemMsgSystemBinding;
import com.fusion.example.model.FusionMessage;
import com.fusion.adapter.delegate.JavaDelegate;

public class JavaMsgSystemDelegate extends JavaDelegate<FusionMessage, ItemMsgSystemBinding> {

    @NonNull
    @Override
    protected ItemMsgSystemBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemMsgSystemBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemMsgSystemBinding binding, @NonNull FusionMessage item, int position) {
        binding.tvSystemMsg.setText(item.getContent());
    }
}
