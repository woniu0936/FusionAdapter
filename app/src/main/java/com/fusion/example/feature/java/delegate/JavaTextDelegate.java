package com.fusion.example.feature.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemTextBinding;
import com.fusion.example.model.TextItem;

public class JavaTextDelegate extends JavaDelegate<TextItem, ItemTextBinding> {

    public JavaTextDelegate() {
        setOnItemClick((binding, item, position) -> {
            Toast.makeText(binding.getRoot().getContext(),
                    "Java Click: " + item.getContent(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    @Nullable
    public Object getUniqueKey(@NonNull TextItem item) {
        return item.getId();
    }

    @NonNull
    @Override
    protected ItemTextBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemTextBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemTextBinding binding, @NonNull TextItem item) {
        binding.tvContent.setText("[Java] " + item.getContent());
    }
}
