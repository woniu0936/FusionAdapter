package com.fusion.adapter.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fusion.adapter.core.R;
import com.fusion.adapter.internal.ClassSignature;
import com.fusion.adapter.internal.FusionViewUtil;
import com.fusion.adapter.internal.ViewSignature;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * [Java 专属] ViewBinding 委托基类
 * <p>
 * 专为 Java 开发者设计，遵循 Android 原生开发习惯：
 * 1. 通过重写 {@link #onCreateBinding} 创建视图。
 * 2. 通过重写 {@link #onBind} 绑定数据。
 *
 * @param <T>  数据类型
 * @param <VB> ViewBinding 类型
 */
public abstract class JavaDelegate<T, VB extends ViewBinding>
        extends FusionDelegate<T, JavaDelegate.JavaBindingHolder<VB>> {

    private Long specificDebounceInterval = null; // null = use global
    private OnItemClickListener<T, VB> clickListener;
    private OnItemLongClickListener<T, VB> longClickListener;

    private final ViewSignature signature = new ClassSignature(this.getClass());

    /**
     * [生命周期] 创建 ViewBinding
     * 类似于 Activity.onCreate 或 Fragment.onCreateView
     *
     * @param inflater LayoutInflater
     * @param parent   父容器
     * @return 初始化的 ViewBinding
     */
    @NonNull
    protected abstract VB onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent);

    /**
     * [生命周期] 绑定数据
     *
     * @param binding  ViewBinding
     * @param item     数据实体
     * @param position 当前索引
     */
    protected abstract void onBind(@NonNull VB binding, @NonNull T item, int position);

    /**
     * [生命周期] 局部刷新 (可选重写)
     */
    protected void onBindPayload(@NonNull VB binding, @NonNull T item, int position, @NonNull List<Object> payloads) {
        onBind(binding, item, position);
    }

    // =======================================================================================
    // RecyclerView 适配层
    // =======================================================================================

    @Override
    public @NotNull ViewSignature getSignature() {
        return signature;
    }

    @NonNull
    @Override
    public final JavaBindingHolder<VB> onCreateViewHolder(@NonNull ViewGroup parent) {
        // 调用子类实现的模板方法
        VB binding = onCreateBinding(LayoutInflater.from(parent.getContext()), parent);
        JavaBindingHolder<VB> holder = new JavaBindingHolder<>(binding);
        FusionViewUtil.setOnClick(holder.itemView, specificDebounceInterval, v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                T item = (T) v.getTag(R.id.fusion_item_tag);
                if (item != null) clickListener.onClick(holder.binding, item, pos);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                T item = (T) v.getTag(R.id.fusion_item_tag);
                if (item != null) return longClickListener.onLongClick(holder.binding, item, pos);
            }
            return false;
        });
        return holder;
    }

    @Override
    public final void onBindViewHolder(@NonNull JavaBindingHolder<VB> holder, @NonNull T item, int position, @NonNull List<Object> payloads) {
        holder.itemView.setTag(R.id.fusion_item_tag, item);
        if (!payloads.isEmpty()) {
            onBindPayload(holder.binding, item, position, payloads);
        } else {
            onBind(holder.binding, item, position);
        }
    }

    // =======================================================================================
    // 事件配置 (Fluent API)
    // =======================================================================================

    // 1. 使用默认(全局)配置
    public void setOnItemClick(@NonNull OnItemClickListener<T, VB> listener) {
        this.clickListener = listener;
        this.specificDebounceInterval = null;
    }

    // 2. 自定义配置
    public void setOnItemClick(long debounceMs, @NonNull OnItemClickListener<T, VB> listener) {
        this.clickListener = listener;
        this.specificDebounceInterval = debounceMs;
    }

    /**
     * 设置长按事件
     */
    public JavaDelegate<T, VB> setOnItemLongClick(@Nullable OnItemLongClickListener<T, VB> listener) {
        this.longClickListener = listener;
        return this;
    }

    // =======================================================================================
    // 内部类与接口
    // =======================================================================================

    @FunctionalInterface
    public interface OnItemClickListener<T, VB extends ViewBinding> {
        void onClick(@NonNull VB binding, @NonNull T item, int position);
    }

    @FunctionalInterface
    public interface OnItemLongClickListener<T, VB extends ViewBinding> {
        boolean onLongClick(@NonNull VB binding, @NonNull T item, int position);
    }

    public static class JavaBindingHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
        @NonNull
        public final VB binding;

        public JavaBindingHolder(@NonNull VB binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}

