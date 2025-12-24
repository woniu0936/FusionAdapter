package com.fusion.adapter

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.TypeRouter

// 这是一个单纯的、高抽象的接口
// 它只关心一件事：把配置好的 Linker 接进来
interface RegistryOwner {
    fun <T : Any> registerRouter(clazz: Class<T>, router: TypeRouter<T>)

    // 注册单类型委托 (Delegate) - 这就是报错缺少的那个方法
    fun <T : Any> registerDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>)
}