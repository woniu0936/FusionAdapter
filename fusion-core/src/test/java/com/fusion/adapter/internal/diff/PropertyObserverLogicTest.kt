package com.fusion.adapter.internal.diff

import com.fusion.adapter.core.GlobalTypeKey
import com.fusion.adapter.core.ViewTypeKey
import com.fusion.adapter.delegate.FusionDelegate
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PropertyObserverLogicTest {

    data class TestModel(val id: Int, val name: String, val age: Int, val gender: String)

    class TestDelegate : FusionDelegate<TestModel, RecyclerView.ViewHolder>() {
        override val viewTypeKey: ViewTypeKey = GlobalTypeKey(TestDelegate::class.java, "test")
        override fun getStableId(item: TestModel): Any = item.id
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = throw UnsupportedOperationException()
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: TestModel, position: Int, payloads: MutableList<Any>) {}
    }

    @Test
    fun `given 1 observer, when property changes, then return single payload`() {
        val delegate = TestDelegate()
        val observer = PropertyObserver1<TestModel, String>({ it.name }) { }
        delegate.addObserver(observer)

        val old = TestModel(1, "A", 20, "M")
        val new = TestModel(1, "B", 20, "M")

        val payload = delegate.getChangePayload(old, new)
        assertThat(payload).isEqualTo(observer)
    }

    @Test
    fun `given 2 observers, when both change, then return list of 2 payloads`() {
        val delegate = TestDelegate()
        val obs1 = PropertyObserver1<TestModel, String>({ it.name }) { }
        val obs2 = PropertyObserver1<TestModel, Int>({ it.age }) { }
        delegate.addObserver(obs1)
        delegate.addObserver(obs2)

        val old = TestModel(1, "A", 20, "M")
        val new = TestModel(1, "B", 21, "M")

        val payload = delegate.getChangePayload(old, new) as List<*>
        assertThat(payload).containsExactly(obs1, obs2)
    }

    @Test
    fun `given 4 observers (extra), when all change, then return list of 4 payloads`() {
        val delegate = TestDelegate()
        val obs1 = PropertyObserver1<TestModel, String>({ it.name }) { }
        val obs2 = PropertyObserver1<TestModel, Int>({ it.age }) { }
        val obs3 = PropertyObserver1<TestModel, String>({ it.gender }) { }
        val obs4 = PropertyObserver1<TestModel, Int>({ it.id }) { }
        
        delegate.addObserver(obs1)
        delegate.addObserver(obs2)
        delegate.addObserver(obs3)
        delegate.addObserver(obs4) // Goes into extraObservers

        val old = TestModel(1, "A", 20, "M")
        val new = TestModel(2, "B", 21, "F")

        val payload = delegate.getChangePayload(old, new) as List<*>
        assertThat(payload).hasSize(4)
        assertThat(payload).containsExactly(obs1, obs2, obs3, obs4)
    }

    @Test
    fun `given many observers, when no properties change, then return null without allocation`() {
        val delegate = TestDelegate()
        repeat(10) {
            delegate.addObserver(PropertyObserver1<TestModel, String>({ it.name }) { })
        }

        val item = TestModel(1, "A", 20, "M")
        val payload = delegate.getChangePayload(item, item)
        
        assertThat(payload).isNull()
    }
}
