package com.fusion.adapter.placeholder

/**
 * 一个全局单例对象，用于在 FusionAdapter / FusionListAdapter 中显式代表“占位符”。
 * 当你向 List 中添加此对象，且通过 registerPlaceholder 注册了布局时，会自动渲染占位符。
 */
object FusionPlaceholder