package com.fusion.example.feature.java;

import static android.view.View.GONE;
import static com.fusion.example.utils.ExtensionsKt.fullStatusBar;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fusion.adapter.FusionListAdapter;
import com.fusion.adapter.internal.TypeDispatcher;
import com.fusion.example.databinding.ActivityRecyclerBinding;
import com.fusion.example.feature.java.delegate.JavaMsgImageDelegate;
import com.fusion.example.feature.java.delegate.JavaMsgSystemDelegate;
import com.fusion.example.feature.java.delegate.JavaMsgTextDelegate;
import com.fusion.example.feature.java.delegate.JavaTextDelegate;
import com.fusion.example.model.FusionMessage;
import com.fusion.example.model.TextItem;
import com.fusion.example.utils.MockDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class JavaInteropActivity extends AppCompatActivity {

    private ActivityRecyclerBinding binding;
    private FusionListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecyclerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.fabAdd.setVisibility(GONE);
        fullStatusBar(this, binding.getRoot());

        // 初始化 Adapter (自动挡)
        adapter = new FusionListAdapter();

        setupRegistration();

        // 设置 RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // 加载数据
        loadData();
    }

    private void setupRegistration() {
        // ========================================================================
        // 场景 1: 一对一注册
        // ========================================================================
        adapter.register(TextItem.class, new JavaTextDelegate());

        // ========================================================================
        // 场景 2: 一对多路由注册 - 使用全新 Dispatcher API
        // ========================================================================
        TypeDispatcher<FusionMessage> messageDispatcher = new TypeDispatcher.Builder<FusionMessage>()
                .uniqueKey(FusionMessage::getId)
                .viewType(FusionMessage::getMsgType)
                .delegate(FusionMessage.TYPE_TEXT, new JavaMsgTextDelegate())
                .delegate(FusionMessage.TYPE_IMAGE, new JavaMsgImageDelegate())
                .delegate(FusionMessage.TYPE_SYSTEM, new JavaMsgSystemDelegate())
                .build();

        adapter.registerDispatcher(FusionMessage.class, messageDispatcher);
    }

    private void loadData() {
        List<Object> items = new ArrayList<>();

        items.add(new TextItem("100", "Hello from Java World"));
        items.add(new TextItem("101", "FusionAdapter v3.0 works great in Java!"));

        List<FusionMessage> chatList = MockDataGenerator.INSTANCE.createChatList(60, 0);
        items.addAll(chatList);

        adapter.submitList(items, null);
    }
}