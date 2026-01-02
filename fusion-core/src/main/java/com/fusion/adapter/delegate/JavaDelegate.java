package com.fusion.adapter.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.fusion.adapter.internal.FusionInternalTags;
import com.fusion.adapter.core.GlobalTypeKey;
import com.fusion.adapter.core.ViewTypeKey;

import java.util.function.Function;

import kotlin.Unit;

/**
 * [JavaDelegate]
 * [Fix #1] 解决了 Java 侧 Payload 分发时的 ClassCastException。
 * [Fix #2] 修复了安全检查漏洞。
 */
public abstract class JavaDelegate<T, VB extends ViewBinding> extends BindingDelegate<T, VB> {

    public JavaDelegate() {
        super(null);
    }

    @NonNull
    @Override
    protected VB onInflateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return onCreateBinding(inflater, parent);
    }

    @NonNull
    @Override
    public ViewTypeKey getViewTypeKey() {
        return new GlobalTypeKey(this.getClass(), FusionInternalTags.TAG_JAVA_DELEGATE);
    }

    @NonNull
    protected abstract VB onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent);

    @Override
    public void onBind(@NonNull VB binding, @NonNull T item, int position) {
        onBind(binding, item);
    }

    protected abstract void onBind(@NonNull VB binding, @NonNull T item);

    // --- Payload 修复 (Fix #1) ---

    public interface PayloadConsumer<VB, P> {
        void accept(VB binding, P val);
    }

    public interface PayloadConsumer2<VB, P1, P2> {
        void accept(VB binding, P1 v1, P2 v2);
    }

    public interface PayloadConsumer3<VB, P1, P2, P3> {
        void accept(VB binding, P1 v1, P2 v2, P3 v3);
    }

    public interface PayloadConsumer4<VB, P1, P2, P3, P4> {
        void accept(VB binding, P1 v1, P2 v2, P3 v3, P4 v4);
    }

    public interface PayloadConsumer5<VB, P1, P2, P3, P4, P5> {
        void accept(VB binding, P1 v1, P2 v2, P3 v3, P4 v4, P5 v5);
    }

    public interface PayloadConsumer6<VB, P1, P2, P3, P4, P5, P6> {
        void accept(VB binding, P1 v1, P2 v2, P3 v3, P4 v4, P5 v5, P6 v6);
    }

    /**
     * [SuppressWarnings] 由于底层 BindingDelegate.addObserver 代理逻辑，
     * 传入的 receiver 已经确保是 VB 类型。
     */
    @SuppressWarnings("unchecked")
    protected final <P> void bindPayload(@NonNull Function<T, P> getter, @NonNull PayloadConsumer<VB, P> consumer) {
        registerPropertyObserver(getter::apply, (holder, val) -> {
            // holder 是 BindingHolder<VB>，需要手动提取 binding
            consumer.accept(holder.getBinding(), (P) val);
            return Unit.INSTANCE;
        });
    }

    @SuppressWarnings("unchecked")
    protected final <P1, P2> void bindPayload(@NonNull Function<T, P1> g1, @NonNull Function<T, P2> g2, @NonNull PayloadConsumer2<VB, P1, P2> consumer) {
        registerPropertyObserver(g1::apply, g2::apply, (holder, v1, v2) -> {
            consumer.accept(holder.getBinding(), (P1) v1, (P2) v2);
            return Unit.INSTANCE;
        });
    }

    @SuppressWarnings("unchecked")
    protected final <P1, P2, P3> void bindPayload(@NonNull Function<T, P1> g1, @NonNull Function<T, P2> g2, @NonNull Function<T, P3> g3, @NonNull PayloadConsumer3<VB, P1, P2, P3> consumer) {
        registerPropertyObserver(g1::apply, g2::apply, g3::apply, (holder, v1, v2, v3) -> {
            consumer.accept(holder.getBinding(), (P1) v1, (P2) v2, (P3) v3);
            return Unit.INSTANCE;
        });
    }

    @SuppressWarnings("unchecked")
    protected final <P1, P2, P3, P4> void bindPayload(@NonNull Function<T, P1> g1, @NonNull Function<T, P2> g2, @NonNull Function<T, P3> g3, @NonNull Function<T, P4> g4, @NonNull PayloadConsumer4<VB, P1, P2, P3, P4> consumer) {
        registerPropertyObserver(g1::apply, g2::apply, g3::apply, g4::apply, (holder, v1, v2, v3, v4) -> {
            consumer.accept(holder.getBinding(), (P1) v1, (P2) v2, (P3) v3, (P4) v4);
            return Unit.INSTANCE;
        });
    }

    @SuppressWarnings("unchecked")
    protected final <P1, P2, P3, P4, P5> void bindPayload(@NonNull Function<T, P1> g1, @NonNull Function<T, P2> g2, @NonNull Function<T, P3> g3, @NonNull Function<T, P4> g4, @NonNull Function<T, P5> g5, @NonNull PayloadConsumer5<VB, P1, P2, P3, P4, P5> consumer) {
        registerPropertyObserver(g1::apply, g2::apply, g3::apply, g4::apply, g5::apply, (holder, v1, v2, v3, v4, v5) -> {
            consumer.accept(holder.getBinding(), (P1) v1, (P2) v2, (P3) v3, (P4) v4, (P5) v5);
            return Unit.INSTANCE;
        });
    }

    @SuppressWarnings("unchecked")
    protected final <P1, P2, P3, P4, P5, P6> void bindPayload(@NonNull Function<T, P1> g1, @NonNull Function<T, P2> g2, @NonNull Function<T, P3> g3, @NonNull Function<T, P4> g4, @NonNull Function<T, P5> g5, @NonNull Function<T, P6> g6, @NonNull PayloadConsumer6<VB, P1, P2, P3, P4, P5, P6> consumer) {
        registerPropertyObserver(g1::apply, g2::apply, g3::apply, g4::apply, g5::apply, g6::apply, (holder, v1, v2, v3, v4, v5, v6) -> {
            consumer.accept(holder.getBinding(), (P1) v1, (P2) v2, (P3) v3, (P4) v4, (P5) v5, (P6) v6);
            return Unit.INSTANCE;
        });
    }
}
