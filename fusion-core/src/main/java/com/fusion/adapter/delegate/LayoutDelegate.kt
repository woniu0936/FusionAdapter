package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.ViewSignature

/**
 * [LayoutDelegate]
 * A delegate implementation designed for legacy migration or simple static layouts.
 *
 * Features:
 * 1. Requires only a LayoutRes ID.
 * 2. Provides a Fluent API via [LayoutHolder] scope.
 * 3. Enforces strict type safety via 'ClassSignature'.
 */
abstract class LayoutDelegate<T : Any>(
    @LayoutRes private val layoutResId: Int
) : FusionDelegate<T, LayoutHolder>() {

    // Identity strategy: The concrete class type itself is the unique signature.
    override val signature: ViewSignature = ClassSignature(this::class.java)

    final override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val holder = LayoutHolder(view)
        onViewHolderCreated(holder)
        return holder
    }

    /**
     * Optional hook for one-time initialization (e.g., setting fixed layout params).
     */
    open fun onViewHolderCreated(holder: LayoutHolder) {}

    /**
     * Binds data to the view.
     *
     * @receiver [LayoutHolder] Provides access to the Fluent API extensions.
     * @param item The data item.
     */
    abstract fun LayoutHolder.onBind(item: T)

    // Bridge method strictly for internal Adapter use
    final override fun onBindViewHolder(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        holder.onBind(item)
    }
}