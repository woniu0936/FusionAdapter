package com.fusion.example.java.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.example.databinding.ItemTextBinding;
import com.fusion.example.model.TextItem;

/**
 * [Java Delegate]
 * 继承 FusionJavaDelegate，体验类似 Activity/Fragment 的写法。
 */
public class JavaTextDelegate extends JavaDelegate<TextItem, ItemTextBinding> {

    public JavaTextDelegate() {
        // 在构造时配置监听，支持 Lambda
        setOnItemClick((binding, item, position) -> {
            Toast.makeText(binding.getRoot().getContext(),
                    "Java Click: " + item.getContent(), Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    protected ItemTextBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        // 标准 ViewBinding 写法
        return ItemTextBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemTextBinding binding, @NonNull TextItem item, int position) {
        binding.tvContent.setText("[Java] " + item.getContent());
    }
}
