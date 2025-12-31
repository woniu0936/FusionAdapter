package com.fusion.adapter.internal.engine

import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.ViewTypeKey
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class FusionCoreIntegrationTest {

    private lateinit var core: FusionCore

    class TestDelegate : FusionDelegate<String, RecyclerView.ViewHolder>() {
        override val viewTypeKey: ViewTypeKey = GlobalTypeKey(String::class.java, "string")
        override fun getStableId(item: String): Any = item
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = mock()
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: String, position: Int, payloads: MutableList<Any>) {}
    }

    @Before
    fun setup() {
        core = FusionCore()
        // 初始化配置为非 Debug 模式，避免测试直接抛出异常，而是检查逻辑
        Fusion.initialize(FusionConfig.Builder().setDebug(false).build())
    }

    @Test
    fun testItemFiltering() {
        // 注册 String 类型的代理
        core.register(String::class.java, TestDelegate())

        val input = listOf("Valid", 123, "Also Valid")
        val filtered = core.filter(input)

        // 验证：Int(123) 应该被剔除，因为没有注册对应的 Delegate
        assert(filtered.size == 2)
        assert(filtered.all { it is String })
    }

    @Test(expected = UnregisteredTypeException::class)
    fun testDebugModeCrash() {
        // 开启 Debug 模式
        Fusion.initialize(FusionConfig.Builder().setDebug(true).build())
        
        // 传入未注册的类型，应该直接抛出异常
        core.filter(listOf(1.23f))
    }
}
