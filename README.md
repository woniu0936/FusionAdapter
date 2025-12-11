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
    implementation("io.github.woniu0936:fusion-core:0.0.1")
}