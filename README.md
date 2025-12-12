
# 🚀 FusionAdapter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.woniu0936/fusion-core)](https://search.maven.org/artifact/io.github.woniu0936/fusion-core)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Paging3](https://img.shields.io/badge/Support-Paging3-green)](https://developer.android.com/topic/libraries/architecture/paging/v3)

[🇨🇳 中文文档](./README_CN.md) | [🇺🇸 English](./README.md)

**FusionAdapter** is a next-generation `RecyclerView` adapter library for Android, built with Kotlin DSL and ViewBinding.

It aims to **fuse** tedious Adapter boilerplate code (ViewHolders, ViewTypes, DiffUtils) into concise, type-safe, and declarative logic. It natively supports **Paging 3** and **Smart Diff**, allowing you to build complex lists with minimal effort.

🔗 **GitHub**: [https://github.com/woniu0936/FusionAdapter](https://github.com/woniu0936/FusionAdapter)

---

## ✨ Features

*   **⚡ Minimalist DSL**: Say goodbye to Adapter class explosions. Launch a list with a single line of code.
*   **🔀 Powerful Routing**:
    *   **Heterogeneous Lists**: Mix different data classes easily (Header + Item + Footer).
    *   **Polymorphism**: Map the *same* data class to *different* layouts based on properties (e.g., Chat bubbles).
*   **📐 Layout Control**: Declare `spanSize` and `fullSpan` directly in the DSL. Perfect for Grids and Staggered Layouts.
*   **🎨 ViewBinding**: Native integration. No more `findViewById`. Type-safe and clean.
*   **🚀 Smart Diff**: Built-in async diffing strategy with `StableId` support to eliminate screen flickering.
*   **📄 Paging 3 Support**: Seamless integration with Jetpack Paging 3 using the exact same API.
*   **🛡️ Production Ready**: Comprehensive global exception interception and fallback view mechanisms to prevent crashes.

---

## 📦 Installation

Add the dependency to your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.woniu0936:fusion-core:0.3.1")
    // optional, support paging3
    implementation("io.github.woniu0936:fusion-paging:0.3.1")
}
```

---

## 🔨 Usage Guide

### 1. Simple List (One-to-One)

The simplest scenario: mapping one data type to one layout.

```kotlin
// In Activity / Fragment
val adapter = recyclerView.setupFusion {
    
    // Register: Data Type (String) -> Layout (ItemTextBinding)
    register(ItemTextBinding::inflate) {
        
        // onBind: Bind data (Receiver is the Binding)
        onBind { item ->
            tvTitle.text = item
        }

        // onClick: Handle click events
        onItemClick { item ->
            toast("Clicked: $item")
        }
    }
}

// Submit data
adapter.submitList(listOf("Hello", "Fusion", "Adapter"))
```

### 2. Multi-Type List: Polymorphism (Chat Mode)

**This is FusionAdapter's killer feature.**
Ideal for scenarios where the data type is the same (e.g., `Message`), but the UI differs based on a property (e.g., Text vs. Image vs. System Notice). No more `getItemViewType`!

```kotlin
data class Message(val id: String, val type: Int, val content: String)

recyclerView.setupFusion {
    // Enable routing mode for the Message type
    register<Message> {
        
        // 1. Define match rule (Extract the Key)
        match { it.type }

        // 2. Map Key -> Layout: Text Message
        map(TYPE_TEXT, ItemMsgTextBinding::inflate) {
            onBind { msg -> 
                tvContent.text = msg.content
                // Apply styles dynamically (Left/Right bubble)
                ChatStyleHelper.bindTextMsg(this, msg.isMe)
            }
        }

        // 3. Map Key -> Layout: Image Message
        map(TYPE_IMAGE, ItemMsgImageBinding::inflate) {
            onBind { msg -> 
                ivImage.load(msg.content)
                ChatStyleHelper.bindImageMsg(this, msg.isMe)
            }
        }

        // 4. Map Key -> Layout: System Notice
        map(TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
            onBind { msg -> tvSystem.text = msg.content }
        }
    }
}
```

### 3. Multi-Type List: Heterogeneous (Mixed Data)

Displaying mixed data entities in a single list, such as: `Header` + `Product` + `Ad` + `Footer`.

```kotlin
recyclerView.setupFusion {
    // Register Header
    register<HeaderItem, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
        onBind { item -> tvTitle.text = item.title }
    }

    // Register Product
    register<ProductItem, ItemProductBinding>(ItemProductBinding::inflate) {
        onBind { item -> tvName.text = item.name }
    }
    
    // Register Ad
    register<AdItem, ItemAdBinding>(ItemAdBinding::inflate) { ... }
}

// Submit mixed list List<Any>
adapter.submitList(listOf(HeaderItem("Hot"), ProductItem(1), AdItem(...)))
```

### 4. Layout Control (Grid & Staggered Support)

Control `GridLayoutManager` or `StaggeredGridLayoutManager` behavior directly within the DSL.

```kotlin
val layoutManager = GridLayoutManager(context, 2) // or StaggeredGridLayoutManager
recyclerView.layoutManager = layoutManager

recyclerView.setupFusion(layoutManager) { // Pass layoutManager to enable layout DSL

    // Header (Full Span)
    register<HeaderItem, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
        onBind { ... }
        
        // Staggered: Enable full span
        fullSpanIf { true } 
        // Grid: Occupy all columns
        spanSize { item, position -> layoutManager.spanCount } 
    }

    // Grid Item (1 Span)
    register<GridItem, ItemGridBinding>(ItemGridBinding::inflate) {
        onBind { ... }
        spanSize { _, _ -> 1 }
    }
}
```

---

## 🚀 Performance Optimization

### 🔹 Smart Diff & StableId

FusionAdapter wraps `AsyncListDiffer` internally. To achieve maximum performance and precise animations (avoiding the flickering of `notifyDataSetChanged`), implementation of the `StableId` interface is recommended:

```kotlin
data class User(
    val uid: String, 
    val name: String
) : StableId {
    // Return a unique identifier.
    // DiffUtil uses this to check if an item has moved or changed.
    override val stableId: Any = uid
}
```

### 🔹 Partial Refresh (Payloads)

Handle `notifyItemChanged(pos, payload)` easily in DSL to refresh only specific views, avoiding image reloading or full redraws.

```kotlin
register(ItemPostBinding::inflate) {
    onBind { post -> 
        tvContent.text = post.content
        updateLikeState(post.isLiked) // Full bind
    }
    
    // Handle partial refresh
    bindPayload(SocialPost::isLiked, SocialPost::likeCount) { isLiked, likeCount ->
        // Triggered ONLY when isLiked or likeCount changes
        updateLikeState(isLiked, likeCount)
    }
}
```

---

## 📄 Paging 3 Support

Fusion provides a dedicated `FusionPagingAdapter`. The API is identical to the standard DSL version, allowing for zero-cost migration.

```kotlin
// Use the setupFusionPaging extension
val pagingAdapter = recyclerView.setupFusionPaging<FusionMessage> {
    register<FusionMessage> {
        match { it.type }
        map(TYPE_TEXT, ItemTextBinding::inflate) { ... }
        map(TYPE_IMAGE, ItemImageBinding::inflate) { ... }
    }
}

// Submit PagingData via ViewModel
lifecycleScope.launch {
    viewModel.flow.collectLatest { pagingData ->
        pagingAdapter.submitData(pagingData)
    }
}
```

---

## ⚙️ Global Configuration

It is recommended to initialize Fusion in your `Application` class to configure Debug mode and global error listeners.

```kotlin
Fusion.initialize {
    // Debug Mode:
    // true  -> Throw exceptions for unregistered types (Recommended for Dev).
    // false -> Render fallback View (Default GONE) to prevent Crashes (Recommended for Prod).
    setDebug(BuildConfig.DEBUG)
    
    // Global Error Listener
    setErrorListener { item, e ->
        Log.e("Fusion", "Rendering error for ${item.javaClass}", e)
        // Report to Crashlytics / Bugly
    }
}
```

---

## ☕ Java Interoperability

FusionAdapter is Java-friendly. You can use the `JavaDelegate` class to mix Java code with Kotlin DSL.

```java
// 1. Create a Delegate
public class UserDelegate extends JavaDelegate<User, ItemUserBinding> {
    // Implement onCreateBinding and onBind ...
}

// 2. Register
adapter.attachDelegate(User.class, new UserDelegate());

// 3. Complex TypeRouter is also supported
adapter.attachLinker(Message.class, new TypeRouter<Message>()
    .match(Message::getType)
    .map(TYPE_TEXT, new TextDelegate())
    .map(TYPE_IMAGE, new ImageDelegate())
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

