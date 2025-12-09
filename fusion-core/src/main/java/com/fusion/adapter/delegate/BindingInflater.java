package com.fusion.adapter.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

/**
 * [SAM] ViewBinding 工厂
 * 作用：解耦 ViewBinding 的创建方式，支持 Java 方法引用 (::inflate)。
 */
@FunctionalInterface
public interface BindingInflater<VB extends ViewBinding> {
    @NonNull
    VB inflate(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, boolean attachToParent);
}
