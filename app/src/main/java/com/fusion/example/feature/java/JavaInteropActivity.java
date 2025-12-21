package com.fusion.example.feature.java;

import static android.view.View.GONE;
import static com.fusion.example.utils.ExtensionsKt.fullStatusBar;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fusion.adapter.FusionListAdapter;
import com.fusion.adapter.internal.TypeRouter;
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
import java.util.function.Function;

import kotlin.jvm.functions.Function1;

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
        // 场景 1: 一对一注册 (Simple Registration)
        // ========================================================================
        // 语法：register(Class).to(Delegate)
        adapter.attachDelegate(TextItem.class, new JavaTextDelegate());

        // ========================================================================
        // 场景 2: 一对多路由注册 (Complex Routing)
        // ========================================================================
        // 语法：register(Class).dispatch(KeyMapper).map(Key, Delegate)...register()
        adapter.attachLinker(FusionMessage.class, new TypeRouter<FusionMessage>()
                .match(FusionMessage::getMsgType)
                .map(FusionMessage.TYPE_TEXT, new JavaMsgTextDelegate())
                .map(FusionMessage.TYPE_IMAGE, new JavaMsgImageDelegate()) // 显式调用 register 完成构建);
                .map(FusionMessage.TYPE_SYSTEM, new JavaMsgSystemDelegate())); // 显式调用 register 完成构建);

    }

    private void loadData() {
        List<Object> items = new ArrayList<>();

        // 1. 添加一些 TextItem
        items.add(new TextItem("100", "Hello from Java World"));
        items.add(new TextItem("101", "FusionAdapter v3.0 works great in Java!"));

        // 2. 添加混合的 FusionMessage (调用 Kotlin 的 Mock 工具)
        // 注意：MockDataGenerator 返回的是 List<FusionMessage>，需要转一下或直接addAll
        List<FusionMessage> chatList = MockDataGenerator.INSTANCE.createChatList(60, 0);
        items.addAll(chatList);

        // 3. 提交数据
        adapter.submitList(items, null);
    }
}
