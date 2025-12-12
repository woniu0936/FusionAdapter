
# 🚀 FusionAdapter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.woniu0936/fusion-core)](https://search.maven.org/artifact/io.github.woniu0936/fusion-core)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Paging3](https://img.shields.io/badge/Support-Paging3-green)](https://developer.android.com/topic/libraries/architecture/paging/v3)

[🇨🇳 中文文档](./README_CN.md) | [🇺🇸 English](./README.md)

**FusionAdapter** 是一个基于 Kotlin DSL 和 ViewBinding 构建的新一代 Android `RecyclerView` 适配器库。

它旨在将繁琐的 Adapter 样板代码（ViewHolder、ViewType、DiffUtil）**熔断（Fusion）** 为简洁、类型安全的声明式代码。不仅原生支持 **Paging 3** 和 **Smart Diff**，更通过极其灵活的 DSL 路由机制，让复杂的多类型列表开发变得轻而易举。

🔗 **GitHub**: [https://github.com/woniu0936/FusionAdapter](https://github.com/woniu0936/FusionAdapter)

---

## ✨ 核心特性

*   **⚡ 极简 DSL**：告别 Adapter 类爆炸，一行代码启动列表，逻辑内聚。
*   **🔀 强大的路由分发**：
    *   支持 **异构列表**（多种不同的 Data Class）。
    *   支持 **同构多态**（同一 Data Class 根据属性映射不同布局，如聊天气泡）。
*   **📐 布局管控**：在 DSL 中直接声明 `spanSize` 和 `fullSpan`，完美适配 Grid 和瀑布流。
*   **🎨 ViewBinding 集成**：原生支持 `ViewBinding`，告别 `findViewById`，类型安全。
*   **🚀 Smart Diff**：内置智能差异计算策略，支持 `StableId`，彻底解决列表闪烁。
*   **📄 Paging 3 支持**：无缝接入 Jetpack Paging 3，API 与普通列表完全一致。
*   **🛡️ 生产级兜底**：完善的全局异常拦截与兜底视图机制，防止 Crash。

---

## 📦 引入依赖

在你的 `build.gradle.kts` (App 模块) 中添加：

```kotlin
dependencies {
    implementation("io.github.woniu0936:fusion-core:0.3.1")
    // 可选，支持paging3
    implementation("io.github.woniu0936:fusion-paging:0.3.1")
}
```

---

## 🔨 使用指南

### 1. 基础列表 (Simple List)

最简单的场景：一种数据对应一种布局。

```kotlin
// 在 Activity / Fragment 中
val adapter = recyclerView.setupFusion {
    
    // 注册: 数据类型 String -> 布局 ItemTextBinding
    register(ItemTextBinding::inflate) {
        
        // onBind: 处理数据绑定 (this 为 Binding)
        onBind { item ->
            tvTitle.text = item
        }

        // onClick: 处理点击事件
        onItemClick { item ->
            toast("Clicked: $item")
        }
    }
}

// 提交数据
adapter.submitList(listOf("Hello", "Fusion", "Adapter"))
```

### 2. 多类型列表：同构多态 (Polymorphism / Chat Mode)

**这是 FusionAdapter 最强大的功能之一。**
适用于数据类型相同（如 `Message`），但需要根据属性（如 `msgType`）展示不同 UI（文本、图片、系统消息）的场景。告别繁琐的 `getItemViewType`！

```kotlin
data class Message(val id: String, val type: Int, val content: String)

recyclerView.setupFusion {
    // 针对 Message 类型开启路由模式
    register<Message> {
        
        // 1. 定义分发规则 (提取 Key)
        match { it.type }

        // 2. 映射: 文本消息
        map(TYPE_TEXT, ItemMsgTextBinding::inflate) {
            onBind { msg -> 
                tvContent.text = msg.content
                // 动态调整气泡样式（左/右）
                ChatStyleHelper.bindTextMsg(this, msg.isMe)
            }
        }

        // 3. 映射: 图片消息
        map(TYPE_IMAGE, ItemMsgImageBinding::inflate) {
            onBind { msg -> 
                ivImage.load(msg.content)
                ChatStyleHelper.bindImageMsg(this, msg.isMe)
            }
        }

        // 4. 映射: 系统通知
        map(TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
            onBind { msg -> tvSystem.text = msg.content }
        }
    }
}
```

### 3. 多类型列表：异构混合 (Heterogeneous List)

在一个列表中混合展示多种不同的数据实体，例如：`Header` + `Product` + `Ad` + `Footer`。

```kotlin
recyclerView.setupFusion {
    // 注册 Header 数据类型
    register<HeaderItem, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
        onBind { item -> tvTitle.text = item.title }
    }

    // 注册商品数据类型
    register<ProductItem, ItemProductBinding>(ItemProductBinding::inflate) {
        onBind { item -> tvName.text = item.name }
    }
    
    // 注册广告数据类型
    register<AdItem, ItemAdBinding>(ItemAdBinding::inflate) { ... }
}

// 提交混合数据列表 List<Any>
adapter.submitList(listOf(HeaderItem("热门"), ProductItem(1), AdItem(...)))
```

### 4. 布局控制 (Grid & Staggered Support)

FusionAdapter 允许你在 DSL 中直接控制 `GridLayoutManager` 或 `StaggeredGridLayoutManager` 的布局行为，无需编写自定义 LayoutManager。

```kotlin
val layoutManager = GridLayoutManager(context, 2) // 或 StaggeredGridLayoutManager
recyclerView.layoutManager = layoutManager

recyclerView.setupFusion(layoutManager) { // 传入 LayoutManager 以启用布局DSL

    // 通栏标题 (占满所有列)
    register<HeaderItem, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
        onBind { ... }
        
        // Staggered: 开启通栏
        fullSpanIf { true } 
        // Grid: 占满 spanCount
        spanSize { item, position -> layoutManager.spanCount } 
    }

    // 普通网格项 (占1列)
    register<GridItem, ItemGridBinding>(ItemGridBinding::inflate) {
        onBind { ... }
        spanSize { _, _ -> 1 }
    }
}
```

---

## 🚀 性能优化

### 🔹 智能 Diff (Smart Diff) & StableId

FusionAdapter 内部封装了 `AsyncListDiffer`。为了获得极致的性能和精准的动画（避免 `notifyDataSetChanged` 带来的闪烁），建议数据模型实现 `StableId` 接口：

```kotlin
data class User(
    val uid: String, 
    val name: String
) : StableId {
    // 返回唯一标识，DiffUtil 将使用它来判断 Item 是否移动或变更
    override val stableId: Any = uid
}
```

### 🔹 局部刷新 (Payloads)

在 DSL 中轻松处理 `notifyItemChanged(pos, payload)`，仅刷新 View 的特定属性，避免图片闪烁或重绘：

```kotlin
register(ItemPostBinding::inflate) {
    onBind { post -> 
        tvContent.text = post.content
        updateLikeState(post.isLiked) // 全量绑定
    }
    
    // 处理局部刷新
    bindPayload(SocialPost::isLiked, SocialPost::likeCount) { isLiked, likeCount ->
        // 仅当 isLiked 或 likeCount 变化时触发
        updateLikeState(isLiked, likeCount)
    }
}
```

---

## 📄 Paging 3 支持

Fusion 提供了专用的 `FusionPagingAdapter`，API 与普通 DSL 版完全一致，零成本迁移：

```kotlin
// 使用 setupFusionPaging 扩展方法
val pagingAdapter = recyclerView.setupFusionPaging<FusionMessage> {
    register<FusionMessage> {
        match { it.type }
        map(TYPE_TEXT, ItemTextBinding::inflate) { ... }
        map(TYPE_IMAGE, ItemImageBinding::inflate) { ... }
    }
}

// 配合 ViewModel 提交 PagingData
lifecycleScope.launch {
    viewModel.flow.collectLatest { pagingData ->
        pagingAdapter.submitData(pagingData)
    }
}
```

---

## ⚙️ 全局配置

建议在 `Application` 中进行初始化，配置 Debug 模式和异常监听。

```kotlin
Fusion.initialize {
    // Debug 模式：
    // true  -> 遇到未注册类型抛出异常 (开发环境推荐，快速发现 Bug)
    // false -> 自动渲染兜底 View (默认 GONE)，防止 Crash (线上环境推荐)
    setDebug(BuildConfig.DEBUG)
    
    // 全局异常监听
    setErrorListener { item, e ->
        Log.e("Fusion", "Rendering error for ${item.javaClass}", e)
    }
}
```

---

## ☕ Java 互操作性

Fusion 并未遗忘 Java 开发者，提供了友好的 `JavaDelegate` 类，支持与 Kotlin DSL 混合使用。

```java
// 1. 创建 Delegate
public class UserDelegate extends JavaDelegate<User, ItemUserBinding> {
    // 实现 onCreateBinding 和 onBind ...
}

// 2. 注册
adapter.attachDelegate(User.class, new UserDelegate());

// 3. 甚至支持复杂的 TypeRouter
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
