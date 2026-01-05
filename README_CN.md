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

FusionAdapter 并非只是另一个 MultiType 库，它是为了解决大型工程中 **“样板代码膨胀”**、**“异步数据不安全”** 以及 **“Paging 3 集成痛苦”** 而生的下一代适配器解决方案。

| 特性 | **FusionAdapter** | **Epoxy (Airbnb)** | **MultiType** | **BRVAH (v4)** |
| :--- | :--- | :--- | :--- | :--- |
| **设计范式** | **响应式 DSL** (原地声明) | 注解驱动 (编译期生成) | 命令式 (类映射) | 继承驱动 (传统的) |
| **样板代码** | **极简 (Zero)**<br>无需创建 Adapter/Holder | 高 (需大量 Model 类) | 中 (需创建 Binder) | 中 (需继承基类) |
| **数据安全** | **Sanitization (数据清洗)**<br>Release 自动剔除坏数据 | 隐式忽略 | 直接崩溃 (Fail-Fast) | 状态不确定 (易错乱) |
| **Paging 3** | **第一方原生支持**<br>自动占位符与 ID 管理 | 需外部扩展库 | 无原生支持 | 兼容模式 |
| **编译损耗** | **零 (纯运行时)** | **显著 (KAPT/KSP)** | 零 | 零 |
| **并发模型** | **Immutable Runtime** | 内部同步 | 线程不安全 | 线程不安全 |
| **学习曲线** | **极低** (即学即用) | 极高 (概念庞杂) | 低 | 中 |

### 💡 核心价值：为什么它适合您的项目？

1.  **不再有 "Class Explosion"**: 传统的方案每增加一种 UI 样式就需要创建一个 `ViewHolder` 或 `ItemBinder` 类。在 FusionAdapter 中，您只需在 DSL 中多写几行代码，极大地保持了代码库的整洁。
2.  **为线上稳定性而生**: 大型项目中，后端返回的异构数据偶尔会包含未定义类型。FusionAdapter 的 **数据清洗机制** 确保了在 Release 环境下，这些非法数据会被安全剔除，而不是导致应用直接闪退。
3.  **零编译负担**: 相比 Epoxy 动辄数秒的注解处理时间，FusionAdapter 全程无编译损耗，让您的构建速度保持飞快。
4.  **完美的动画体验**: 结合 FNV-1a 64位哈希算法生成的 **级联 Stable ID**，即使在复杂的 Paging 异步加载场景下，也能提供教科书级的 RecyclerView 插入/删除动画。

---

## ✨ 核心特性

*   **⚡ 极简 DSL**: 告别 Adapter 类爆炸。仅需一个代码块即可启动一个多类型列表。
*   **🛡️ 健壮的数据清洗**:
    *   **Debug**: 遇到未注册类型立即崩溃 (Fail-Fast)，帮助在开发期发现 Bug。
    *   **Release**: 自动剔除非法数据 (Fail-Safe)，防止线上崩溃或 Grid 布局错位。
*   **🧵 高并发安全**: 采用 **Immutable Runtime** 设计，确保多线程环境下的数据读写安全与极致性能。
*   **📄 原生 Paging 3**: 提供专用的 `FusionPagingAdapter`，与标准 Paging API 深度集成。支持 **确定性占位符 ID**，解决刷新抖动。
*   **🔀 级联 Stable ID 策略**:
    *   支持 **Router 级 (共享)** 和 **Delegate 级 (覆盖)** 的 ID 配置策略。
    *   内置 FNV-1a 64位哈希算法，彻底解决跨类型 ID 碰撞。
*   **📐 智能布局控制**: 直接在 DSL 中声明 `spanSize` 和 `fullSpan`，自动适配 Grid 和瀑布流。
*   **🚀 内存与日志安全**: 
    *   **自动泄露防护**: 在 `onViewRecycled` 时自动清理视图 Tag，严防内存泄漏。
    *   **企业级日志**: 高性能异步日志系统。支持通过 ProGuard 在 Release 包中自动剥离调试日志代码。
*   **☕ Java 友好**: 不仅支持 Kotlin DSL，还为 Java 开发者提供了完整的 **Builder 模式** 支持。

---

## 📦 安装

在模块级 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation("io.github.woniu0936:fusion-core:0.8.0")
    // 可选：原生 Paging 3 支持
    implementation("io.github.woniu0936:fusion-paging:0.8.0")
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
        
        // [Level 1] Router 级配置: 
        // 默认情况下，所有 Message 的 ID 都是 it.id
        stableId { it.id }

        // 定义匹配规则
        match { it.type }

        // [Inherit] 继承: 自动继承 Router 级的 stableId
        map(TYPE_TEXT, ItemMsgTextBinding::inflate) {
            onBind { msg -> ... }
        }

        // [Override] 覆盖: 特殊情况覆盖默认 ID 规则
        // 例如：将同一条消息拆分显示，防止 ID 冲突
        map(TYPE_TIMELINE, ItemTimeLineBinding::inflate) {
            // [Level 2] Delegate 级配置: 优先级高于 Router 级
            stableId { "${it.id}_time" }
            onBind { msg -> ... }
        }
    }
}
```

### 3. Paging 3 集成

专为 Paging 3 设计的适配器，无缝接入。

```kotlin
val pagingAdapter = FusionPagingAdapter<User>()

pagingAdapter.apply {
    // 常规注册
    register(ItemUserBinding::inflate) {
        onBind { user -> ... }
    }
    
    // 可选：注册占位符 (骨架屏)
    registerPlaceholder(ItemSkeletonBinding::inflate) {
        onBind { /* 配置加载动画 */ }
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

### 5. Java 互操作性 (Builder 模式)

Fusion 对 Java 开发者同样友好。您可以使用 `TypeRouter.Builder` 来实现类型安全的注册。

```java
// Java 示例
FusionAdapter adapter = new FusionAdapter();

// 使用 Builder 模式配置路由
TypeRouter<User> userRouter = new TypeRouter.Builder<User>()
    .match(user -> user.getRole())
    .map("ADMIN", new AdminDelegate())
    .map("USER", new UserDelegate())
    .build();

adapter.register(User.class, userRouter);
recyclerView.setAdapter(adapter);
```

---

## ⚙️ 进阶特性

### 1. 局部刷新与属性级绑定 (Payloads)

通过 `onPayload` 结合 Kotlin 属性引用，FusionAdapter 实现了 **"精确到 View"** 的更新。只有变化的属性才会触发代码执行，彻底解决复杂 Item 的刷新抖动问题。

```kotlin
register<Post>(ItemPostBinding::inflate) {
    onBind { post -> /* 全量绑定 */ }

    // 【单属性监听】仅当 likeCount 变化时，仅更新点赞数 View
    onPayload(Post::likeCount) { count ->
        tvLikeCount.text = count.toString()
    }

    // 【多属性联合监听】当头像或昵称任一变化时，触发闭包
    onPayload(Post::avatar, Post::nickname) { avatar, name ->
        ivAvatar.load(avatar)
        tvName.text = name
    }
}
```

### 2. 级联 Stable ID：解决动画冲突的终极方案

在处理 **"同一个数据对象渲染为多个列表项"** (例如：IM 消息被拆分为时间线和气泡) 时，普通的 ID 会导致 RecyclerView 动画错乱。Fusion 提供级联 ID 策略：

```kotlin
register<Message> {
    // [Level 1] Router 级：默认所有子项使用 ID 字段
    stableId { it.id } 

    map(TYPE_TEXT, ItemTextBinding::inflate) {
        onBind { ... } // 继承 Level 1 的 ID
    }

    map(TYPE_TIMELINE, ItemTimeBinding::inflate) {
        // [Level 2] Delegate 级：覆盖 Router 级，防止 ID 冲突
        stableId { "${it.id}_time" } 
        onBind { ... }
    }
}
```

### 3. 手动骨架屏控制 (Skeleton API)

非 Paging 模式下，您可以像操作普通数据一样操作占位符：

```kotlin
// 1. 注册占位符样式
adapter.registerPlaceholder(ItemSkeletonBinding::inflate) {
    onBind { /* 配置骨架屏动画 */ }
}

// 2. 显示占位符（骨架屏模式）
adapter.showPlaceholders(count = 10)

// 3. 异步数据回来后，直接清除
adapter.clearPlaceholders()
adapter.setItems(realData)
```

---

## ☕ Java 互操作性

FusionAdapter 为 Java 开发者提供了完整的适配支持。

```java
// 1. 实现 Delegate
public class UserDelegate extends JavaDelegate<User, ItemUserBinding> {
    @Override
    public Object getStableId(@NonNull User item) {
        return item.getId();
    }

    @Override
    protected ItemUserBinding onCreateBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemUserBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void onBind(@NonNull ItemUserBinding binding, @NonNull User item) {
        binding.tvName.setText(item.getName());
    }

    @Override
    protected void onCreate(@NonNull ItemUserBinding binding) {
        // [Fix #1] 修复 Java 侧属性监听，解决多维度刷新
        bindPayload(User::getName, (binding, name) -> binding.tvName.setText(name));
    }
}

// 2. 注册
adapter.register(User.class, new TypeRouter.Builder<User>()
    .stableId(User::getId)
    .map("DEFAULT", new UserDelegate())
    .build()
);
```

---

## 🛡️ 健壮性与安全

FusionAdapter 引入了严格的 **Sanitization (数据清洗)** 机制来确保布局的一致性。

### 全局配置
建议在 `Application` 中初始化 Fusion：

```kotlin
Fusion.initialize {
    setDebug(BuildConfig.DEBUG) // Debug 模式 Fail-Fast，Release 模式 Safe-Drop
    setErrorListener { item, e -> 
        // 监控未注册类型或数据异常
        Log.e("Fusion", "Error on item: $item", e)
    }
}
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