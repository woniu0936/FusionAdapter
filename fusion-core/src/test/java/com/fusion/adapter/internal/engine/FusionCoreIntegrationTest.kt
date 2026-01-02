package com.fusion.adapter.internal.engine

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.ViewTypeKey
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FusionCoreIntegrationTest {

    private lateinit var core: FusionCore

    class TestDelegate : FusionDelegate<String, RecyclerView.ViewHolder>() {
        override val viewTypeKey: ViewTypeKey = GlobalTypeKey(String::class.java, "string")
        override fun getStableId(item: String): Any = item
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = mockk()
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: String, position: Int, payloads: MutableList<Any>) {}
    }

    @BeforeEach
    fun setup() {
        core = FusionCore()
        // Initialize config as non-Debug mode to avoid crash during logical checks unless intended
        Fusion.initialize(FusionConfig.Builder().setDebug(false).build())
    }

    @Test
    fun `given registered delegate, when filtering list, then unregistered items should be removed`() {
        // Arrange
        core.register(String::class.java, TestDelegate())
        val input = listOf("Valid", 123, "Also Valid")

        // Act
        val filtered = core.filter(input)

        // Assert: Int(123) should be removed because no Delegate is registered for it
        assertThat(filtered).hasSize(2)
        assertThat(filtered).containsExactly("Valid", "Also Valid").inOrder()
        assertThat(filtered.all { it is String }).isTrue()
    }

    @Test
    fun `given debug mode enabled, when filtering unregistered items, then it should throw UnregisteredTypeException`() {
        // Arrange
        Fusion.initialize(FusionConfig.Builder().setDebug(true).build())
        
        // Act & Assert
        assertThrows<UnregisteredTypeException> {
            core.filter(listOf(1.23f))
        }
    }
}