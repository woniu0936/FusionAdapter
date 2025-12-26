package com.fusion.example.feature.lab;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fusion.adapter.FusionListAdapter;
import com.fusion.adapter.internal.TypeDispatcher;
import com.fusion.adapter.placeholder.FusionPlaceholderExtensionsKt;
import com.fusion.example.databinding.ActivityBaseFixedBinding;
import com.fusion.example.databinding.ItemLabPlaceholderBinding;
import com.fusion.example.databinding.ItemLabRecordBinding;
import com.fusion.example.feature.lab.delegate.JavaMsgTextDelegate;
import com.fusion.example.core.model.ChatMessage;
import com.fusion.example.utils.ExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;

public class LabJavaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBaseFixedBinding b = ActivityBaseFixedBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        ExtensionsKt.fullStatusBar(this, b.getRoot());
        b.toolbar.setTitle("Java Interop Lab");

        FusionListAdapter adapter = new FusionListAdapter();
        
        TypeDispatcher<ChatMessage> d = new TypeDispatcher.Builder<ChatMessage>()
                .uniqueKey(ChatMessage::getId)
                .viewType(it -> 1)
                .delegate(1, new JavaMsgTextDelegate())
                .build();
        adapter.registerDispatcher(ChatMessage.class, d);

        // [API] 使用专属骨架屏 (Java 端调用)
        adapter.registerPlaceholder(ItemLabPlaceholderBinding::inflate, (scope) -> {
            scope.onBind(binding -> {

            });
        });

        b.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerView.setAdapter(adapter);

        // 使用扩展函数
        FusionPlaceholderExtensionsKt.showPlaceholders(adapter, 10);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Object> items = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                items.add(new ChatMessage("java_obj_" + i, "Decoupled Java Implementation #" + i, 1, true, null));
            }
            adapter.submitList(items, null);
        }, 1200);
    }
}
