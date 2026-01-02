package com.fusion.adapter.interop;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.fusion.adapter.delegate.JavaDelegate;
import com.fusion.adapter.internal.GlobalTypeKey;

import org.junit.jupiter.api.Test;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

public class JavaInteropTest {

    static class TestItem {
        String name;
        public TestItem(String name) { this.name = name; }
        public String getName() { return name; }
    }

    static class TestBinding implements ViewBinding {
        @NonNull
        @Override
        public android.view.View getRoot() {
            return mock(android.view.View.class);
        }
    }

    static class TestJavaDelegate extends JavaDelegate<TestItem, TestBinding> {

        @NonNull
        @Override
        protected TestBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            return new TestBinding();
        }

        @Override
        protected void onBind(@NonNull TestBinding binding, @NonNull TestItem item) {
            // Bind logic
        }
        
        @Override
        public Object getStableId(@NonNull TestItem item) {
            return item.getName();
        }
    }

    @Test
    public void givenJavaDelegate_whenInstantiated_thenItShouldHaveCorrectTypeKey() {
        // Arrange
        TestJavaDelegate delegate = new TestJavaDelegate();

        // Act
        GlobalTypeKey key = (GlobalTypeKey) delegate.getViewTypeKey();

        // Assert
        assertThat(key.getPrimary()).isEqualTo(TestJavaDelegate.class);
        assertThat(key.getSecondary()).isEqualTo("Fusion:JavaDelegate");
    }
}
