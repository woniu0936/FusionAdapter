
# 🚀 FusionAdapter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.woniu0936/fusion-core)](https://search.maven.org/artifact/io.github.woniu0936/fusion-core)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Paging3](https://img.shields.io/badge/Support-Paging3-green)](https://developer.android.com/topic/libraries/architecture/paging/v3)

[🇨🇳 中文文档](./README_CN.md) | [🇺🇸 English](./README.md)

**FusionAdapter** is a next-generation `RecyclerView` adapter library for Android, built with Kotlin DSL and ViewBinding.

It aims to **fuse** the tedious boilerplate code of traditional Adapters (ViewHolders, ViewTypes, DiffUtils) into concise, type-safe, and declarative logic. It natively supports **Paging 3** and **Smart Diff**.

🔗 **GitHub**: [https://github.com/woniu0936/FusionAdapter](https://github.com/woniu0936/FusionAdapter)

---

## ✨ Features

*   **⚡ Minimalist DSL**: Say goodbye to repetitive Adapter classes. Initialize a list in a single line.
*   **🔒 Type Safe**: Generic-based routing ensures type safety at compile time.
*   **🎨 ViewBinding Integration**: Native support for `ViewBinding`, eliminating `findViewById`.
*   **🔀 Flexible Routing**: Easily handle One-to-One (Simple) and One-to-Many (Polymorphic) lists.
*   **🚀 Smart Diff**: Built-in intelligent diffing strategy with `StableId` support to eliminate list flickering.
*   **📄 Paging 3 Support**: Seamless integration with Jetpack Paging 3 using the same consistent API.
*   **🛡️ Production-Grade Safety**: Comprehensive global exception interception and fallback view mechanisms to prevent crashes.

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

## 🔨 Quick Start

### 1. Basic List (One-to-One)

The most common scenario: one data type maps to one layout.

```kotlin
// In Activity or Fragment
val adapter = recyclerView.setupFusion {
    
    // Register: Data Type (String) -> Layout (ItemTextBinding)
    register(ItemTextBinding::inflate) {
        
        // Bind Data (DSL receiver is the Binding)
        onBind { item ->
            tvTitle.text = item
        }

        // Handle Click
        onClick { item ->
            toast("Clicked: $item")
        }
    }
}

// Submit Data
adapter.submitList(listOf("Hello", "Fusion", "Adapter"))
```

### 2. Polymorphic List (One-to-Many)

Ideal for complex screens, such as a chat interface where a single `Message` type displays differently based on properties (e.g., Text vs. Image).

```kotlin
data class Message(val type: Int, val content: String)

recyclerView.setupFusion {
    
    // Register routing rules for the Message type
    register<Message> {
        // 1. Define Match Rule (Extract Key from Item)
        match { it.type }

        // 2. Map Key -> Layout & Logic
        map(TYPE_TEXT, ItemTextBinding::inflate) {
            onBind { msg -> tvContent.text = msg.content }
        }

        map(TYPE_IMAGE, ItemImageBinding::inflate) {
            onBind { msg -> ivImage.load(msg.content) }
        }
    }
}
```

---

## 🚀 Advanced Usage

### 🔹 Smart Diff & StableId

FusionAdapter encapsulates `AsyncListDiffer`. For ultimate performance and precise animations (avoiding the flicker of `notifyDataSetChanged`), implement the `StableId` interface in your data models:

```kotlin
data class User(
    val uid: String, 
    val name: String
) : StableId {
    // Return a unique identifier for DiffUtil
    override val stableId: Any = uid
}
```

### 🔹 Paging 3 Support

Fusion provides a dedicated `FusionPagingAdapter`. The API remains identical to the standard version, ensuring zero migration cost.

```kotlin
// Use the 'setupFusionPaging' extension
val pagingAdapter = recyclerView.setupFusionPaging<MyItem> {
    register(ItemUserBinding::inflate) {
        onBind { user -> ... }
    }
}

// Collect PagingData from ViewModel
lifecycleScope.launch {
    viewModel.flow.collectLatest { pagingData ->
        pagingAdapter.submitData(pagingData)
    }
}
```

### 🔹 Partial Updates (Payloads)

Handle `notifyItemChanged(pos, payload)` effortlessly within the DSL to avoid redrawing the entire Item:

```kotlin
register(ItemUserBinding::inflate) {
    onBind { user -> 
        tvName.text = user.name
        tvStatus.text = user.status 
    }
    
    // Handle Partial Refresh
    onBindPayload { user, payloads ->
        // payloads is a List<Any>
        if (payloads.contains("STATUS_CHANGED")) {
            tvStatus.text = user.status
        }
    }
}
```

---

## ⚙️ Global Configuration (Optional)

It is recommended to initialize Fusion in your `Application` class to configure Debug mode and global error listeners.

```kotlin
Fusion.initialize {
    // Debug Mode:
    // true  -> Throw exceptions for unregistered types (Recommended for Development)
    // false -> Show fallback View (Default GONE) to prevent Crashes (Recommended for Production)
    setDebug(BuildConfig.DEBUG)
    
    // Monitor exceptions in Production
    setErrorListener { item, e ->
        // Report to Bugly / Firebase / Sentry
        CrashReport.postCatchedException(e)
    }
    
    // (Optional) Custom Global Fallback Delegate
    setGlobalFallback(MyCustomErrorDelegate())
}
```

---

## ☕ Java Support

Fusion provides a developer-friendly `JavaDelegate` class for Java users.

```java
// 1. Create a Delegate
public class UserDelegate extends JavaDelegate<User, ItemUserBinding> {
    @Override
    protected ItemUserBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent) {
        return ItemUserBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(ItemUserBinding binding, User item, int position) {
        binding.tvName.setText(item.name);
    }
}

// 2. Register
adapter.attachDelegate(User.class, new UserDelegate());
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

