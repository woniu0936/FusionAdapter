package com.fusion.adapter.internal

/**
 * [PropertyObserver]
 * 属性观察者接口。
 */
interface PropertyObserver<T> {
    fun checkChange(oldItem: T, newItem: T): Any?
    fun execute(receiver: Any, item: T)
}

internal class PropertyObserver1<T, P>(val g1: (T) -> P, val action: Any.(P) -> Unit) : PropertyObserver<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? = if (g1(oldItem) != g1(newItem)) this else null
    override fun execute(receiver: Any, item: T) { receiver.action(g1(item)) }
}

internal class PropertyObserver2<T, P1, P2>(val g1: (T) -> P1, val g2: (T) -> P2, val action: Any.(P1, P2) -> Unit) : PropertyObserver<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? = if (g1(oldItem) != g1(newItem) || g2(oldItem) != g2(newItem)) this else null
    override fun execute(receiver: Any, item: T) { receiver.action(g1(item), g2(item)) }
}

internal class PropertyObserver3<T, P1, P2, P3>(val g1: (T) -> P1, val g2: (T) -> P2, val g3: (T) -> P3, val action: Any.(P1, P2, P3) -> Unit) : PropertyObserver<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? = if (g1(oldItem) != g1(newItem) || g2(oldItem) != g2(newItem) || g3(oldItem) != g3(newItem)) this else null
    override fun execute(receiver: Any, item: T) { receiver.action(g1(item), g2(item), g3(item)) }
}

internal class PropertyObserver4<T, P1, P2, P3, P4>(val g1: (T) -> P1, val g2: (T) -> P2, val g3: (T) -> P3, val g4: (T) -> P4, val action: Any.(P1, P2, P3, P4) -> Unit) : PropertyObserver<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? = if (g1(oldItem) != g1(newItem) || g2(oldItem) != g2(newItem) || g3(oldItem) != g3(newItem) || g4(oldItem) != g4(newItem)) this else null
    override fun execute(receiver: Any, item: T) { receiver.action(g1(item), g2(item), g3(item), g4(item)) }
}

internal class PropertyObserver5<T, P1, P2, P3, P4, P5>(val g1: (T) -> P1, val g2: (T) -> P2, val g3: (T) -> P3, val g4: (T) -> P4, val g5: (T) -> P5, val action: Any.(P1, P2, P3, P4, P5) -> Unit) : PropertyObserver<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? = if (g1(oldItem) != g1(newItem) || g2(oldItem) != g2(newItem) || g3(oldItem) != g3(newItem) || g4(oldItem) != g4(newItem) || g5(oldItem) != g5(newItem)) this else null
    override fun execute(receiver: Any, item: T) { receiver.action(g1(item), g2(item), g3(item), g4(item), g5(item)) }
}

internal class PropertyObserver6<T, P1, P2, P3, P4, P5, P6>(val g1: (T) -> P1, val g2: (T) -> P2, val g3: (T) -> P3, val g4: (T) -> P4, val g5: (T) -> P5, val g6: (T) -> P6, val action: Any.(P1, P2, P3, P4, P5, P6) -> Unit) : PropertyObserver<T> {
    override fun checkChange(oldItem: T, newItem: T): Any? = if (g1(oldItem) != g1(newItem) || g2(oldItem) != g2(newItem) || g3(oldItem) != g3(newItem) || g4(oldItem) != g4(newItem) || g5(oldItem) != g5(newItem) || g6(oldItem) != g6(newItem)) this else null
    override fun execute(receiver: Any, item: T) { receiver.action(g1(item), g2(item), g3(item), g4(item), g5(item), g6(item)) }
}
