package com.fusion.adapter

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.TypeDispatcher

/**
 * [FusionRegistry]
 */
interface FusionRegistry {
    fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>)
    fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>)
}
