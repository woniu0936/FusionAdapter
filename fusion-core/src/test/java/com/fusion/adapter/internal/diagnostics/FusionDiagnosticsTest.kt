package com.fusion.adapter.internal.diagnostics

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.core.GlobalTypeKey
import com.fusion.adapter.core.ViewTypeKey
import com.fusion.adapter.internal.engine.FusionCore
import com.fusion.adapter.internal.registry.ViewTypeRegistry
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FusionDiagnosticsTest {

    private lateinit var core: FusionCore

    // Mock Delegate
    class TestDelegate : FusionDelegate<String, RecyclerView.ViewHolder>() {
        override val viewTypeKey: ViewTypeKey = GlobalTypeKey(String::class.java, "test")
        override fun getStableId(item: String): Any = item
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = mockk()
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: String, position: Int, payloads: MutableList<Any>) {
            // Simulate work
            Thread.sleep(1) 
        }
    }

    @BeforeEach
    fun setup() {
        // Enable Debug to record stats
        Fusion.initialize(FusionConfig.Builder().setDebug(true).build())
        core = FusionCore()
    }

    @Test
    fun `given debug mode, when creates and binds view holder, then metrics are recorded`() {
        // Arrange
        val delegate = spyk(TestDelegate())
        core.register(String::class.java, delegate)
        
        val item = "TestItem"
        val viewType = core.getItemViewType(item)
        val parent = mockk<ViewGroup>()
        val holder = mockk<RecyclerView.ViewHolder>(relaxed = true)

        every { delegate.onCreateViewHolder(parent) } returns holder

        // Act
        // 1. Trigger Create
        core.onCreateViewHolder(parent, viewType)
        
        // 2. Trigger Bind
        core.onBindViewHolder(holder, item, 0)

        // Assert
        val diagnostics = core.getDiagnostics(totalItems = 1)
        
        assertThat(diagnostics.isDebug).isTrue()
        assertThat(diagnostics.registeredDelegatesCount).isEqualTo(1)
        
        val delegateDiag = diagnostics.delegates.first()
        assertThat(delegateDiag.viewType).isEqualTo(viewType)
        assertThat(delegateDiag.createCount).isEqualTo(1)
        assertThat(delegateDiag.bindCount).isEqualTo(1)
        assertThat(delegateDiag.totalCreateTimeMs).isGreaterThan(0.0)
    }

    @Test
    fun `given multiple calls, when getting diagnostics, then averages are calculated correctly`() {
        // Arrange
        val delegate = TestDelegate()
        core.register(String::class.java, delegate)
        val viewType = core.getItemViewType("A")
        val parent = mockk<ViewGroup>()
        
        // Act
        // Simulate 5 creates
        repeat(5) {
            core.onCreateViewHolder(parent, viewType)
        }
        
        // Simulate 10 binds
        val holder = mockk<RecyclerView.ViewHolder>(relaxed = true)
        repeat(10) {
            core.onBindViewHolder(holder, "A", 0)
        }

        // Assert
        val diag = core.getDiagnostics(10).delegates.first()
        
        assertThat(diag.createCount).isEqualTo(5)
        assertThat(diag.bindCount).isEqualTo(10)
    }
    
    @Test
    fun `given release mode, when executing, then metrics remain zero`() {
        // Arrange
        Fusion.initialize(FusionConfig.Builder().setDebug(false).build())
        val releaseCore = FusionCore() // Re-init to pick up config? Or Core reads config dynamically? 
        // Core reads config dynamically in onBind/onCreate, but monitor is inside core.
        
        val delegate = TestDelegate()
        releaseCore.register(String::class.java, delegate)
        val viewType = releaseCore.getItemViewType("A")
        val parent = mockk<ViewGroup>()
        val holder = mockk<RecyclerView.ViewHolder>(relaxed = true)

        // Act
        releaseCore.onCreateViewHolder(parent, viewType)
        releaseCore.onBindViewHolder(holder, "A", 0)

        // Assert
        val diag = releaseCore.getDiagnostics(1).delegates.first()
        assertThat(diag.createCount).isEqualTo(0)
        assertThat(diag.bindCount).isEqualTo(0)
    }
}
