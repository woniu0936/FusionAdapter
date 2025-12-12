package com.fusion.adapter.internal

import androidx.annotation.RestrictTo

/**
 * 观察者基类接口
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Watcher<T> {
    // 检查是否有变化，有变化返回 this，无变化返回 null
    fun checkChange(oldItem: T, newItem: T): Any?

    // 执行更新逻辑 (Receiver 在运行时动态传入)
    fun execute(receiver: Any, item: T)
}

/**
 * 单参数观察者 (优化性能)
 */
internal class PropertyWatcher1<T, R, P>(
    private val getter: (T) -> P,
    private val action: R.(P) -> Unit
) : Watcher<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? {
        // 使用 getter 函数，兼容 KProperty 和 Java Lambda
        return if (getter(oldItem) != getter(newItem)) this else null
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(receiver: Any, item: T) {
        (receiver as R).action(getter(item))
    }
}

/**
 * 双参数观察者
 */
internal class PropertyWatcher2<T, R, P1, P2>(
    private val getter1: (T) -> P1,
    private val getter2: (T) -> P2,
    private val action: R.(P1, P2) -> Unit
) : Watcher<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? {
        if (getter1(oldItem) != getter1(newItem)) return this
        if (getter2(oldItem) != getter2(newItem)) return this
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(receiver: Any, item: T) {
        (receiver as R).action(getter1(item), getter2(item))
    }
}

internal class PropertyWatcher3<T, R, P1, P2, P3>(
    private val getter1: (T) -> P1,
    private val getter2: (T) -> P2,
    private val getter3: (T) -> P3,
    private val action: R.(P1, P2, P3) -> Unit
) : Watcher<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? {
        if (getter1(oldItem) != getter1(newItem)) return this
        if (getter2(oldItem) != getter2(newItem)) return this
        if (getter3(oldItem) != getter3(newItem)) return this
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(receiver: Any, item: T) {
        (receiver as R).action(getter1(item), getter2(item), getter3(item))
    }
}

internal class PropertyWatcher4<T, R, P1, P2, P3, P4>(
    private val getter1: (T) -> P1,
    private val getter2: (T) -> P2,
    private val getter3: (T) -> P3,
    private val getter4: (T) -> P4,
    private val action: R.(P1, P2, P3, P4) -> Unit
) : Watcher<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? {
        if (getter1(oldItem) != getter1(newItem)) return this
        if (getter2(oldItem) != getter2(newItem)) return this
        if (getter3(oldItem) != getter3(newItem)) return this
        if (getter4(oldItem) != getter4(newItem)) return this
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(receiver: Any, item: T) {
        (receiver as R).action(getter1(item), getter2(item), getter3(item), getter4(item))
    }
}

internal class PropertyWatcher5<T, R, P1, P2, P3, P4, P5>(
    private val getter1: (T) -> P1,
    private val getter2: (T) -> P2,
    private val getter3: (T) -> P3,
    private val getter4: (T) -> P4,
    private val getter5: (T) -> P5,
    private val action: R.(P1, P2, P3, P4, P5) -> Unit
) : Watcher<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? {
        if (getter1(oldItem) != getter1(newItem)) return this
        if (getter2(oldItem) != getter2(newItem)) return this
        if (getter3(oldItem) != getter3(newItem)) return this
        if (getter4(oldItem) != getter4(newItem)) return this
        if (getter5(oldItem) != getter5(newItem)) return this
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(receiver: Any, item: T) {
        (receiver as R).action(getter1(item), getter2(item), getter3(item), getter4(item), getter5(item))
    }
}

internal class PropertyWatcher6<T, R, P1, P2, P3, P4, P5, P6>(
    private val getter1: (T) -> P1,
    private val getter2: (T) -> P2,
    private val getter3: (T) -> P3,
    private val getter4: (T) -> P4,
    private val getter5: (T) -> P5,
    private val getter6: (T) -> P6,
    private val action: R.(P1, P2, P3, P4, P5, P6) -> Unit
) : Watcher<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? {
        if (getter1(oldItem) != getter1(newItem)) return this
        if (getter2(oldItem) != getter2(newItem)) return this
        if (getter3(oldItem) != getter3(newItem)) return this
        if (getter4(oldItem) != getter4(newItem)) return this
        if (getter5(oldItem) != getter5(newItem)) return this
        if (getter6(oldItem) != getter6(newItem)) return this
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(receiver: Any, item: T) {
        (receiver as R).action(getter1(item), getter2(item), getter3(item), getter4(item), getter5(item), getter6(item))
    }
}