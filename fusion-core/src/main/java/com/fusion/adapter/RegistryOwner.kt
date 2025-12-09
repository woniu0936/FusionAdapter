package com.fusion.adapter

import com.fusion.adapter.internal.TypeRouter

// 这是一个单纯的、高抽象的接口
// 它只关心一件事：把配置好的 Linker 接进来
interface RegistryOwner {
    fun <T : Any> attachLinker(clazz: Class<T>, linker: TypeRouter<T>)
}