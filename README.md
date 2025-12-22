
# 🚀 FusionAdapter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.woniu0936/fusion-core)](https://search.maven.org/artifact/io.github.woniu0936/fusion-core)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Paging3](https://img.shields.io/badge/Support-Paging3-green)](https://developer.android.com/topic/libraries/architecture/paging/v3)

[🇨🇳 中文文档](./README_CN.md) | [🇺🇸 English](./README.md)

**FusionAdapter** is a modern, fail-safe RecyclerView adapter library designed for the Kotlin era.

It eliminates the tedious boilerplate of ViewHolders, ViewTypes, and DiffUtils, allowing you to build complex heterogeneous lists using a concise **Kotlin DSL**. With built-in **Data Sanitization**, **Native Paging 3 Support**, and **ViewBinding**, it bridges the gap between your data and UI with zero friction.

🔗 **GitHub**: [https://github.com/woniu0936/FusionAdapter](https://github.com/woniu0936/FusionAdapter)

---

## 🆚 Why FusionAdapter?

How does FusionAdapter compare to other industry-standard libraries?

| Feature | **FusionAdapter** | **Epoxy (Airbnb)** | **MultiType** | **BRVAH (v4)** |
| :--- | :--- | :--- | :--- | :--- |
| **Paradigm** | **Kotlin DSL** (In-place) | Annotation Processing | Class Mapping | Inheritance |
| **Boilerplate** | **Zero** (Lambda based) | High (Models required) | Medium (Binders required) | Medium |
| **Safety Strategy** | **Fail-Fast / Fail-Safe**<br>*(Crash in Debug, Drop in Release)* | Fail-Safe (Implicit) | Fail-Fast (Crash) | Undefined |
| **Paging 3** | **Native Support**<br>*(Unified API, Auto Placeholders)* | Extension Lib required | None | Compatibility Mode |
| **Build Impact** | **None** (Runtime only) | **Slow** (KAPT/KSP) | None | None |
| **Grid Layout** | **Automatic Injection** | Automatic | Manual | Manual |

**Key Takeaway:** FusionAdapter offers the power and flexibility of **Epoxy** without the build-time penalty, combined with the simplicity of **MultiType**, but with a modern DSL and best-in-class **Paging 3** integration.

---

## ✨ Key Features

*   **⚡ Minimalist DSL**: Launch a multi-type list in a single block of code. No more creating separate Adapter/ViewHolder classes.
*   **🛡️ Robust Sanitization**:
    *   **Debug**: Crashes immediately on unregistered types to catch bugs early (Fail-Fast).
    *   **Release**: Silently drops invalid data to prevent crashes and layout corruption (Fail-Safe).
*   **📄 Native Paging 3**: A dedicated `FusionPagingAdapter` that works exactly like the standard adapter. Supports **automatic null placeholders**.
*   **🔀 Cascading Stable ID Strategy**:
    *   Supports **Router-level (Shared)** and **Delegate-level (Override)** ID configurations.
    *   Resolves ID conflicts in One-to-Many scenarios and supports perfect RecyclerView animations.
*   **🔀 Powerful Routing**: Map a single data class to multiple layouts based on properties (e.g., Chat bubbles).
*   **📐 Layout Intelligence**: Declare `spanSize` and `fullSpan` logic directly within the item configuration.
*   **🚀 Smart Diff**: Built-in `AsyncListDiffer` with `StableId` support for high-performance animations.
*   **🎨 ViewBinding**: Type-safe view access. No `findViewById`.

---

## 📦 Installation

Add the dependency to your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.woniu0936:fusion-core:0.6.0")
    // Optional: Native Paging 3 support
    implementation("io.github.woniu0936:fusion-paging:0.6.0")
}
```

---

## 🔨 Usage Guide

### 1. Simple List (DSL)

Map a data type to a layout and bind it.

```kotlin
// In Activity / Fragment
val adapter = recyclerView.setupFusion {
    
    // Register: Data Type (String) -> Layout (ItemTextBinding)
    register(ItemTextBinding::inflate) {
        
        // Configure Stable ID for performance (Optional)
        stableId { it }
        
        // onBind: 'this' is the ViewBinding, 'item' is the data
        onBind { item ->
            tvTitle.text = item
        }

        // onItemClick: Handle click events
        onItemClick { item ->
            toast("Clicked: $item")
        }
    }
}

// Submit list
adapter.submitList(listOf("Hello", "Fusion", "Adapter"))
```

### 2. Polymorphism (Cascading Stable ID)

Handle scenarios where the same data class (`Message`) renders differently based on its state.
Fusion introduces a **"Cascading Priority Strategy"** to handle IDs elegantly.

```kotlin
data class Message(val id: Long, val type: Int, val content: String)

recyclerView.setupFusion {
    register<Message> {
        
        // [Level 2] Router-level configuration:
        // By default, all Message delegates use 'it.id'
        stableId { it.id }

        // Define routing rules
        match { it.type }

        // [Inherit]: Automatically inherits the Router-level stableId
        map(TYPE_TEXT, ItemMsgTextBinding::inflate) {
            onBind { msg -> ... }
        }

        map(TYPE_IMAGE, ItemMsgImageBinding::inflate) {
            onBind { msg -> ... }
        }

        // [Override]: Override the default ID rule for special cases
        // e.g., Split a single message into parts to avoid ID collision
        map(TYPE_SPLIT_PART, ItemMsgSplitBinding::inflate) {
            // [Level 1] Delegate-level configuration: Higher priority
            stableId { "${it.id}_split" }
            onBind { msg -> ... }
        }
    }
}
```

### 3. Paging 3 Integration

Fusion provides `FusionPagingAdapter` which shares the **exact same DSL** API.

```kotlin
// Use setupFusionPaging extension
val pagingAdapter = recyclerView.setupFusionPaging<User> {
    
    // 1. Register Normal Item
    register(ItemUserBinding::inflate) {
        stableId { it.userId }
        onBind { user -> tvName.text = user.name }
    }

    // 2. Register Placeholder (Skeleton)
    // Automatically shown when Paging 3 returns null (loading state)
    registerPlaceholder(ItemSkeletonBinding::inflate) {
        onBind { binding.shimmer.startShimmer() }
    }
}

// Submit PagingData
lifecycleScope.launch {
    viewModel.pagingFlow.collectLatest { pagingData ->
        pagingAdapter.submitData(pagingData)
    }
}
```

### 4. Grid & Staggered Layouts

Control span sizes directly in the item registration. Fusion automatically handles the `SpanSizeLookup`.

```kotlin
val layoutManager = GridLayoutManager(context, 2)
recyclerView.layoutManager = layoutManager

// Pass layoutManager to enable Layout DSL
recyclerView.setupFusion(layoutManager) {
    
    // Header: Always occupy full width
    register<Header>(ItemHeaderBinding::inflate) {
        onBind { ... }
        // Works for both Grid and Staggered layouts
        fullSpanIf { true } 
    }

    // Grid Item: Dynamic span size
    register<GridItem>(ItemGridBinding::inflate) {
        onBind { ... }
        spanSize { item, position, scope -> 
            // Promote specific items to full width
            if (item.isPromoted) scope.totalSpans else 1 
        }
    }
}
```

---

## 🛡️ Robustness & Safety

FusionAdapter introduces a strict **Sanitization** mechanism to ensure layout consistency.

### Global Configuration
Initialize Fusion in your `Application` class:

```kotlin
Fusion.initialize {
    // [DEBUG Mode]: Fail-Fast
    // CRASH immediately when an unregistered type is encountered.
    // Forces developers to fix bugs during development.
    setDebug(BuildConfig.DEBUG)
    
    // [RELEASE Mode]: Fail-Safe
    // Silently DROP unregistered items to prevent crashes or grid layout corruption.
    // Report dropped items via listener for analytics.
    setErrorListener { item, e ->
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    // Enable Default Stable ID Check (Recommended)
    // Enforce that all registered types must provide a stableId for best performance.
    setDefaultStableIds(true)
}
```

---

## ⚙️ Advanced Features

### Partial Refresh (Payloads)
Update only specific views without re-binding the entire row.

```kotlin
register(ItemPostBinding::inflate) {
    onBind { post -> ... } // Full bind
    
    // Automatically triggers when 'likeCount' changes
    bindPayload(Post::likeCount) { count ->
        tvLikeCount.text = count.toString()
    }
}
```

### Manual Placeholders (Non-Paging)
Drive skeleton screens explicitly in a standard list.

```kotlin
// 1. Register placeholder layout
adapter.registerPlaceholder(ItemSkeletonBinding::inflate)

// 2. Show 10 skeleton items
adapter.submitPlaceholders(10)

// 3. Data loaded, show real data
adapter.submitList(data)
```

---

## ☕ Java Interoperability

FusionAdapter is Java-friendly. You can use the `JavaDelegate` class.

```java
// 1. Create a Delegate
public class UserDelegate extends JavaDelegate<User, ItemUserBinding> {
    // Implement onCreateBinding and onBind...
}

// 2. Register
adapter.attachLinker(User.class, new TypeRouter<User>()
    .stableId(user -> user.getId()) // Java 8 Lambda configuration
        .map(null, new UserDelegate())
        );
```

---

## 📄 License

```
Copyright 2024 FusionAdapter Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```