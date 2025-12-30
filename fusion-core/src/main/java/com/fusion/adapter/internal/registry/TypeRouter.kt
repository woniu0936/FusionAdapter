package com.fusion.adapter.internal.registry

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.FusionInternalTags.TAG_ROUTER_AUTO
import com.fusion.adapter.internal.FusionInternalTags.TAG_ROUTER_DEFAULT
import com.fusion.adapter.internal.GlobalTypeKey

/**
 * [TypeRouter]
 * 核心路由引擎。
 */
class TypeRouter<T : Any> private constructor(
    private val viewTypeKeyExtractor: (T) -> Any?,
    private val stableIdExtractor: ((T) -> Any?)?,
    private val keyToDelegate: Map<Any, FusionDelegate<T, *>>
) {

    fun select(item: T): FusionDelegate<T, *>? {
        val key = viewTypeKeyExtractor(item) ?: return null
        return keyToDelegate[key]
    }

    fun getAllDelegates(): Collection<FusionDelegate<T, *>> = keyToDelegate.values

    fun getStableId(item: T): Any? = stableIdExtractor?.invoke(item)

    /**
     * [Builder]
     */
    class Builder<T : Any> {
        private var viewTypeKeyExtractor: ((T) -> Any?)? = null
        private var stableIdExtractor: ((T) -> Any?)? = null
        private val keyToDelegate = HashMap<Any, FusionDelegate<T, *>>()

        fun match(extractor: (T) -> Any?): Builder<T> {
            this.viewTypeKeyExtractor = extractor
            return this
        }

        fun stableId(extractor: (T) -> Any?): Builder<T> {
            this.stableIdExtractor = extractor
            return this
        }

        fun map(key: Any, delegate: FusionDelegate<T, *>): Builder<T> {
            keyToDelegate[key] = delegate
            return this
        }

        fun build(): TypeRouter<T> {
            val extractor = viewTypeKeyExtractor ?: { GlobalTypeKey(it::class.java, TAG_ROUTER_DEFAULT) }
            return TypeRouter(extractor, stableIdExtractor, keyToDelegate)
        }
    }

    companion object {
        fun <T : Any> create(delegate: FusionDelegate<T, *>): TypeRouter<T> {
            val key = GlobalTypeKey(delegate::class.java, TAG_ROUTER_AUTO)
            return Builder<T>()
                .map(key, delegate)
                .match { key }
                .build()
        }
    }
}