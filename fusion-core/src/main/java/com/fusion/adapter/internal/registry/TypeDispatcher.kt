package com.fusion.adapter.internal.registry

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.FusionInternalTags.TAG_DISPATCHER_AUTO
import com.fusion.adapter.internal.FusionInternalTags.TAG_DISPATCHER_DEFAULT
import com.fusion.adapter.internal.GlobalTypeKey

/**
 * [TypeDispatcher]
 * 核心路由引擎。
 */
class TypeDispatcher<T : Any> private constructor(
    private val viewTypeKeyExtractor: (T) -> Any?,
    private val uniqueKeyExtractor: ((T) -> Any?)?,
    private val keyToDelegate: Map<Any, FusionDelegate<T, *>>
) {

    fun select(item: T): FusionDelegate<T, *>? {
        val key = viewTypeKeyExtractor(item) ?: return null
        return keyToDelegate[key]
    }

    fun getAllDelegates(): Collection<FusionDelegate<T, *>> = keyToDelegate.values

    fun getUniqueKey(item: T): Any? = uniqueKeyExtractor?.invoke(item)

    /**
     * [Builder]
     */
    class Builder<T : Any> {
        private var viewTypeKeyExtractor: ((T) -> Any?)? = null
        private var uniqueKeyExtractor: ((T) -> Any?)? = null
        private val keyToDelegate = HashMap<Any, FusionDelegate<T, *>>()

        fun viewType(extractor: (T) -> Any?): Builder<T> {
            this.viewTypeKeyExtractor = extractor
            return this
        }

        fun uniqueKey(extractor: (T) -> Any?): Builder<T> {
            this.uniqueKeyExtractor = extractor
            return this
        }

        fun delegate(key: Any, delegate: FusionDelegate<T, *>): Builder<T> {
            keyToDelegate[key] = delegate
            return this
        }

        fun build(): TypeDispatcher<T> {
            val extractor = viewTypeKeyExtractor ?: { GlobalTypeKey(it::class.java, TAG_DISPATCHER_DEFAULT) }
            return TypeDispatcher(extractor, uniqueKeyExtractor, keyToDelegate)
        }
    }

    companion object {
        fun <T : Any> create(delegate: FusionDelegate<T, *>): TypeDispatcher<T> {
            val key = GlobalTypeKey(delegate::class.java, TAG_DISPATCHER_AUTO)
            return Builder<T>()
                .delegate(key, delegate)
                .viewType { key }
                .build()
        }
    }
}