
# 🚀 FusionAdapter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.woniu0936/fusion-core)](https://search.maven.org/artifact/io.github.woniu0936/fusion-core)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Paging3](https://img.shields.io/badge/Support-Paging3-green)](https://developer.android.com/topic/libraries/architecture/paging/v3)

[🇨🇳 中文文档](./README_CN.md) | [🇺🇸 English](./README.md)

**FusionAdapter** 是一款专为 Kotlin 时代设计的现代化、Fail-Safe 的 RecyclerView 适配器库。

它旨在通过简洁的 **Kotlin DSL** 消除繁琐的样板代码（ViewHolders, ViewTypes, DiffUtils）。结合内置的 **高性能数据清洗 (Sanitization)**、**原生 Paging 3 支持** 以及 **ViewBinding**，它让构建复杂的多类型异构列表变得前所未有的简单与安全。

🔗 **GitHub**: [https://github.com/woniu0936/FusionAdapter](https://github.com/woniu0936/FusionAdapter)

---

## 🆚 为什么选择 FusionAdapter?

FusionAdapter 与业界主流库的深度对比：

| 核心维度 | **FusionAdapter** | **Epoxy (Airbnb)** | **MultiType** | **BRVAH (v4)** |
| :--- | :--- | :--- | :--- | :--- |
| **编码范式** | **Kotlin DSL** (原地注册) | 注解处理 (Annotation) | 类映射 (Class Mapping) | 继承式 (Inheritance) |
| **样板代码** | **零 (Zero)**<br>无需创建 Adapter/Holder 类 | 高 (需创建 Model) | 中 (需创建 Binder) | 中 |
| **安全策略** | **Fail-Fast / Fail-Safe**<br>*(Debug 崩溃提醒，Release 自动清洗)* | Fail-Safe (隐式不显示) | Fail-Fast (直接崩溃) | 未定义<br>(可能导致错乱) |
| **Paging 3** | **原生支持**<br>*(统一 API，自动处理占位符)* | 需引入扩展库 | 无支持 | 兼容模式<br>(核心仍为回调) |
| **编译成本** | **无** (纯运行时) | **慢** (依赖 KAPT/KSP) | 无 | 无 |
| **网格布局** | **自动注入** (Auto Span) | 自动处理 | 手动管理 | 手动管理 |

**核心优势:** FusionAdapter 拥有 **Epoxy** 般的强大布局能力却无编译损耗，拥有 **MultiType** 般的灵活性却提供更现代的 DSL，同时为您提供了梦寐以求的顶级 **Paging 3** 原生支持。

---

## ✨ 核心特性

*   **⚡ 极简 DSL**: 告别 Adapter 类爆炸。仅需一个代码块即可启动一个多类型列表。
*   **🛡️ 健壮的数据清洗**:
    *   **Debug**: 遇到未注册类型立即崩溃 (Fail-Fast)，帮助在开发期发现 Bug。
    *   **Release**: 自动剔除非法数据 (Fail-Safe)，防止线上崩溃或 Grid 布局错位。
*   **📄 原生 Paging 3**: 提供专用的 `FusionPagingAdapter`，API 与标准版完全一致。支持 **自动 Null 占位符**。
*   **🔀 级联 Stable ID 策略**:
    *   支持 **Router 级 (共享)** 和 **Delegate 级 (覆盖)** 的 ID 配置策略。
    *   解决一对多场景下的 ID 冲突，完美支持 RecyclerView 动画。
*   **📐 智能布局控制**: 直接在 DSL 中声明 `spanSize` 和 `fullSpan`，自动适配 Grid 和瀑布流。
*   **🚀 智能差分**: 内置 `AsyncListDiffer`，结合 Stable ID 实现高性能渲染。
*   **🎨 ViewBinding**: 类型安全，告别 `findViewById`。

---

## 📦 安装

在模块级 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation("io.github.woniu0936:fusion-core:0.6.0")
    // 可选：原生 Paging 3 支持
    implementation("io.github.woniu0936:fusion-paging:0.6.0")
}
```

---

## 🔨 使用指南

### 1. 简单列表 (DSL)

最简单的场景：将一种数据类型映射到一个布局。

```kotlin
// In Activity / Fragment
val adapter = recyclerView.setupFusion {
    
    // 注册: 数据类型 (String) -> 布局 (ItemTextBinding)
    register(ItemTextBinding::inflate) {
        
        // 配置 Stable ID 以优化性能 (可选)
        stableId { it } 

        // onBind: `this` 是 ViewBinding, `item` 是数据
        onBind { item ->
            tvTitle.text = item
        }

        // onItemClick: 处理点击事件
        onItemClick { item ->
            toast("Clicked: $item")
        }
    }
}

// 提交数据
adapter.submitList(listOf("Hello", "Fusion", "Adapter"))
```

### 2. 多态列表 (级联 Stable ID)

处理同一数据类 (`Message`) 根据内部状态渲染不同布局的场景。
Fusion 引入了 **"级联优先策略"** 来优雅处理 ID 问题。

```kotlin
data class Message(val id: Long, val type: Int, val content: String)

recyclerView.setupFusion {
    register<Message> {
        
        // [Level 2] Router 级配置: 
        // 默认情况下，所有 Message 的 ID 都是 it.id
        stableId { it.id }

        // 定义匹配规则
        match { it.type }

        // [Inherit] 继承: 自动继承 Router 级的 stableId
        map(TYPE_TEXT, ItemMsgTextBinding::inflate) {
            onBind { msg -> ... }
        }

        map(TYPE_IMAGE, ItemMsgImageBinding::inflate) {
            onBind { msg -> ... }
        }

        // [Override] 覆盖: 特殊情况覆盖默认 ID 规则
        // 例如：将同一条消息拆分显示，防止 ID 冲突
        map(TYPE_SPLIT_PART, ItemMsgSplitBinding::inflate) {
            // [Level 1] Delegate 级配置: 优先级高于 Router 级
            stableId { "${it.id}_split" }
            onBind { msg -> ... }
        }
    }
}
```

### 3. Paging 3 集成

Fusion 提供了 `FusionPagingAdapter`，API 与标准版 DSL 完全一致，零成本迁移。

```kotlin
// 使用 setupFusionPaging 扩展方法
val pagingAdapter = recyclerView.setupFusionPaging<User> {
    
    // 1. 注册正常 Item
    register(ItemUserBinding::inflate) {
        stableId { it.userId }
        onBind { user -> tvName.text = user.name }
    }

    // 2. 注册占位符 (骨架屏)
    // 当 Paging 3 返回 null (加载中) 时自动显示此布局
    registerPlaceholder(ItemSkeletonBinding::inflate) {
        onBind { binding.shimmer.startShimmer() }
    }
}

// 提交 PagingData
lifecycleScope.launch {
    viewModel.pagingFlow.collectLatest { pagingData ->
        pagingAdapter.submitData(pagingData)
    }
}
```

### 4. 网格与瀑布流布局

直接在 DSL 中控制 Span。Fusion 会自动处理 `SpanSizeLookup`，无需手动计算 Position。

```kotlin
val layoutManager = GridLayoutManager(context, 2)
recyclerView.layoutManager = layoutManager

// 传入 layoutManager 以启用布局 DSL
recyclerView.setupFusion(layoutManager) {
    
    // Header: 总是占满一行
    register<Header>(ItemHeaderBinding::inflate) {
        onBind { ... }
        // 适用于 Grid 和 瀑布流
        fullSpanIf { true } 
    }

    // Grid Item: 动态 Span
    register<GridItem>(ItemGridBinding::inflate) {
        onBind { ... }
        spanSize { item, position, scope -> 
            // 如果是推广商品占满一行，否则占一格
            if (item.isPromoted) scope.totalSpans else 1 
        }
    }
}
```

---

## 🛡️ 健壮性与安全

FusionAdapter 引入了严格的 **Sanitization (数据清洗)** 机制来确保布局的一致性。

### 全局配置
建议在 `Application` 中初始化 Fusion：

```kotlin
Fusion.initialize {
    // [DEBUG 模式]: Fail-Fast 
    // 遇到未注册类型立即 CRASH。强制开发者在开发阶段修复问题。
    setDebug(BuildConfig.DEBUG)
    
    // [RELEASE 模式]: Fail-Safe 
    // 静默丢弃未注册的数据，防止线上 Crash 或 Grid 布局错位。
    // 通过监听器上报异常数据以便分析。
    setErrorListener { item, e ->
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    // 开启默认 StableId 检查 (推荐)
    // 强制要求所有注册类型必须提供 stableId，以获得最佳性能
    setDefaultStableIds(true) 
}
```

---

## ⚙️ 进阶特性

### 局部刷新 (Payloads)
轻松处理 `notifyItemChanged(pos, payload)`，仅更新变化的 View。

```kotlin
register(ItemPostBinding::inflate) {
    onBind { post -> ... } // 全量更新
    
    // 仅当 likeCount 发生变化时触发
    bindPayload(Post::likeCount) { count ->
        tvLikeCount.text = count.toString()
    }
}
```

### 手动骨架屏 (非 Paging)
在普通列表中显式驱动骨架屏显示。

```kotlin
// 1. 注册骨架屏布局
adapter.registerPlaceholder(ItemSkeletonBinding::inflate)

// 2. 显示 10 个骨架占位符
adapter.submitPlaceholders(10)

// 3. 数据加载完毕，显示真实数据
adapter.submitList(data)
```

---

## ☕ Java 互操作性

FusionAdapter 对 Java 友好。你可以通过继承 `JavaDelegate` 类来混合使用。

```java
// 1. 创建 Delegate
public class UserDelegate extends JavaDelegate<User, ItemUserBinding> {
    // 实现 onCreateBinding 和 onBind ...
}

// 2. 注册
adapter.attachLinker(User.class, new TypeRouter<User>()
    .stableId(user -> user.getId()) // Java 8 Lambda 配置 ID
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