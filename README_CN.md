
# 🚀 FusionAdapter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.woniu0936/fusion-core)](https://search.maven.org/artifact/io.github.woniu0936/fusion-core)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Paging3](https://img.shields.io/badge/Support-Paging3-green)](https://developer.android.com/topic/libraries/architecture/paging/v3)

[🇨🇳 中文文档](./README_CN.md) | [🇺🇸 English](./README.md)

**FusionAdapter** 是一个基于 Kotlin DSL 和 ViewBinding 构建的新一代 Android `RecyclerView` 适配器库。

它旨在将繁琐的 Adapter 样板代码（ViewHolder、ViewType、DiffUtil）**熔断（Fusion）** 为简洁、类型安全的声明式代码，同时原生支持 **Paging 3** 和 **Smart Diff**。

🔗 **GitHub**: [https://github.com/woniu0936/FusionAdapter](https://github.com/woniu0936/FusionAdapter)

---

## ✨ 核心特性

*   **⚡ 极简 DSL**：告别重复的 Adapter 类定义，一行代码启动列表。
*   **🔒 类型安全**：基于泛型的路由分发，编译期保障类型正确。
*   **🎨 ViewBinding 集成**：原生支持 `ViewBinding`，告别 `findViewById`。
*   **🔀 灵活路由**：轻松处理 一对一（单类型）和 一对多（多态/复杂类型）列表。
*   **🚀 Smart Diff**：内置智能差异计算策略，支持 `StableId`，彻底解决列表闪烁。
*   **📄 Paging 3 支持**：无缝接入 Jetpack Paging 3，API 与普通列表完全一致。
*   **🛡️ 生产级兜底**：完善的全局异常拦截与兜底视图机制，防止 Crash。

---

## 📦 引入依赖

在你的 `build.gradle.kts` (App 模块) 中添加：

```kotlin
dependencies {
    implementation("io.github.woniu0936:fusion-core:0.2.0")
    // 可选，支持paging3
    implementation("io.github.woniu0936:fusion-paging:0.2.0")
}
```

---

## 🔨 快速上手

### 1. 基础列表 (One-to-One)

最常见的场景：一种数据对应一种布局。

```kotlin
// 在 Activity / Fragment 中
val adapter = recyclerView.setupFusion {
    
    // 注册: 数据类型 String -> 布局 ItemTextBinding
    register(ItemTextBinding::inflate) {
        
        // 绑定数据 (dsl receiver 是 Binding)
        onBind { item ->
            tvTitle.text = item
        }

        // 点击事件
        onClick { item ->
            toast("Clicked: $item")
        }
    }
}

// 提交数据
adapter.submitList(listOf("Hello", "Fusion", "Adapter"))
```

### 2. 多类型列表 (Polymorphism)

适用于复杂页面，例如聊天列表（同一数据类型 `Message`，根据属性显示文本或图片）。

```kotlin
data class Message(val type: Int, val content: String)

recyclerView.setupFusion {
    
    // 注册 Message 类型的路由规则
    register<Message> {
        // 1. 定义匹配规则 (从 Item 中提取 Key)
        match { it.type }

        // 2. 映射 Key -> 布局 & 逻辑
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

## 🚀 进阶功能

### 🔹 智能 Diff (Smart Diff) & StableId

FusionAdapter 内部封装了 `AsyncListDiffer`。为了获得极致的性能和精准的动画（避免 `notifyDataSetChanged` 带来的闪烁），建议数据模型实现 `StableId` 接口：

```kotlin
data class User(
    val uid: String, 
    val name: String
) : StableId {
    // 返回唯一标识，用于 DiffUtil 判断是否是同一个 Item
    override val stableId: Any = uid
}
```

### 🔹 Paging 3 支持

Fusion 提供了专用的 `FusionPagingAdapter`，API 与普通版完全一致，零成本迁移：

```kotlin
// 使用 setupFusionPaging 扩展方法
val pagingAdapter = recyclerView.setupFusionPaging<MyItem> {
    register(ItemUserBinding::inflate) {
        onBind { user -> ... }
    }
}

// 配合 ViewModel 提交 PagingData
lifecycleScope.launch {
    viewModel.flow.collectLatest { pagingData ->
        pagingAdapter.submitData(pagingData)
    }
}
```

### 🔹 局部刷新 (Payloads)

在 DSL 中轻松处理 `notifyItemChanged(pos, payload)` 带来的局部刷新，避免整个 Item 重绘：

```kotlin
register(ItemUserBinding::inflate) {
    onBind { user -> 
        tvName.text = user.name
        tvStatus.text = user.status 
    }
    
    // 处理局部刷新
    onBindPayload { user, payloads ->
        // payloads 是一个 List<Any>
        if (payloads.contains("STATUS_CHANGED")) {
            tvStatus.text = user.status
        }
    }
}
```

---

## ⚙️ 全局配置 (Optional)

建议在 `Application` 中进行初始化，配置 Debug 模式和异常监听。

```kotlin
Fusion.initialize {
    // Debug 模式：
    // true  -> 遇到未注册类型抛出异常 (开发环境推荐)
    // false -> 显示兜底 View (默认 GONE)，防止 Crash (线上环境推荐)
    setDebug(BuildConfig.DEBUG)
    
    // 线上环境监控异常
    setErrorListener { item, e ->
        // 上报到 Bugly / Firebase
        CrashReport.postCatchedException(e)
    }
    
    // (可选) 自定义全局兜底样式
    setGlobalFallback(MyCustomErrorDelegate())
}
```

---

## ☕ Java 支持

Fusion 为 Java 开发者提供了友好的 `JavaDelegate` 类。

```java
// 1. 创建 Delegate
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

// 2. 注册
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
