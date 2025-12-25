package com.fusion.adapter.internal

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.lang.reflect.Modifier

/**
 * [ViewTypeStorage]
 * 全局视图类型注册中心。
 *
 * 核心职能：
 * 1. 确保相同的 Delegate 逻辑在全局范围内获得一致且唯一的 ViewType ID。
 * 2. [Safety] 使用强引用 Map 防止 ID 在页面销毁后丢失，解决跨页面复用时的 Crash 问题。
 * 3. [Guard] 内置 Key 稳定性检查，防止动态 Key 导致的内存泄漏。
 */
internal object ViewTypeStorage {

    // 保证 ID 的生命周期与 App 进程一致，避免 Adapter 被 GC 后 ID 丢失导致的 "Unknown ViewType" 崩溃。
    // Map 的增长上限由 App 中的代码类数量决定（有界增长），不会无限膨胀。
    private val keyToViewType = ConcurrentHashMap<Any, Int>()

    private val atomicId = AtomicInteger(10000)

    /**
     * 获取或生成全局唯一的 ViewType ID。
     */
    fun getViewType(key: Any): Int {
        // [架构防御] 在分配 ID 前，进行稳定性检查
        // 如果 Key 是不稳定的（例如每次打开页面都会变），直接 Crash 提醒开发者
        checkKeyStability(key)

        return keyToViewType.computeIfAbsent(key) {
            atomicId.getAndIncrement()
        }
    }

    /**
     * 稳定性检查器
     * 杜绝使用匿名内部类、局部对象、非静态内部类或原生 Object 作为 Key。
     */
    private fun checkKeyStability(key: Any) {
        // 1. 解包：如果是我们内部封装的 Key，取出核心标识符进行检查
        val actualKey = when (key) {
            is DslTypeKey -> key.identifier
            is ClassTypeKey -> key.delegateClass
            else -> key
        }

        // 2. 白名单：基本类型、Class、String、Enum 绝对安全
        if (actualKey is Number ||
            actualKey is String ||
            actualKey is Enum<*> ||
            actualKey is Class<*>) {
            return
        }

        // 3. 黑名单检查：检查自定义对象的类特征
        val clazz = actualKey.javaClass

        // 🚫 禁止原生 Object (new Object() 或 Any())
        // 它们的 equals/hashCode 默认依赖内存地址，极不稳定
        if (clazz == Any::class.java || clazz == java.lang.Object::class.java) {
            throwDescription(actualKey, "Raw Object (no equals/hashCode)")
        }

        // 🚫 禁止匿名内部类 (object : Key {})
        // 每次执行都会生成新的 Class 类型，导致 Map 快速膨胀
        if (clazz.isAnonymousClass) {
            throwDescription(actualKey, "Anonymous Inner Class")
        }

        // 🚫 禁止局部类 (在方法内部定义的 class)
        if (clazz.isLocalClass) {
            throwDescription(actualKey, "Local Class")
        }

        // 🚫 禁止非静态内部类 (inner class)
        // 它们的实例隐式持有外部类引用，equals 通常依赖外部状态
        if (clazz.isMemberClass && !Modifier.isStatic(clazz.modifiers)) {
            throwDescription(actualKey, "Non-static Member Class")
        }

        // ✅ 通过检查：Key 是顶层类、静态内部类或数据类，且不是原生 Object。
    }

    private fun throwDescription(key: Any, reason: String) {
        throw IllegalStateException(
            "Fusion: Unstable ViewType Key detected! \n" +
                    "Key: $key (${key.javaClass.name})\n" +
                    "Reason: Key is a '$reason'.\n" +
                    "To prevent memory leaks (unbounded Map growth), ViewType Keys MUST be globally stable constants.\n" +
                    "Allowed: Primitives, Strings, Enums, Classes, Objects, or static Data Classes."
        )
    }
}