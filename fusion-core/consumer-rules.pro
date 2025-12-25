################################################################################
# FusionAdapter Consumer Rules
#
# 这些规则会自动合并到使用本库的 App 的混淆配置中。
# 参考了 Hilt, Retrofit, OkHttp 等顶级库的最佳实践。
################################################################################

# ----------------------------------------------------------------------------
# 1. 基础属性保持 (Essential Attributes)
# ----------------------------------------------------------------------------
# Fusion 依赖 Class<?> 作为 Key 进行路由匹配 (TypeRouter)。
# 我们必须保留泛型签名 (Signature)，否则 List<String> 和 List<Int> 在运行时可能无法区分，
# 或者继承关系检查 (findLinkerForInheritance) 会因为泛型擦除而失效。
# InnerClasses 和 EnclosingMethod 对于正确解析 Kotlin 的 Lambda 和内部类至关重要。
-keepattributes Signature, InnerClasses, EnclosingMethod

# 保留注解，防止 @RestrictTo 或其他运行时注解被移除
-keepattributes *Annotation*

# 保留调试信息（可选，但在库中保留源文件和行号有助于用户反馈 Crash）
-keepattributes SourceFile, LineNumberTable

# ----------------------------------------------------------------------------
# 2. Kotlin 元数据 (Kotlin Metadata)
# ----------------------------------------------------------------------------
# Fusion 大量使用 Kotlin 的 inline fun, reified 关键字以及属性代理。
# 为了保证 Kotlin 反射 (KClass) 和标准库函数的正常工作，必须保留 Metadata。
-keep class kotlin.Metadata { *; }

# ----------------------------------------------------------------------------
# 3. ViewBinding 支持 (ViewBinding Support)
# ----------------------------------------------------------------------------
# Fusion 的 DSL (RegistrationBuilder.bind) 经常接收 ViewBinding::inflate 方法引用。
# 在 Release 模式下，R8 可能会认为这些静态 inflate 方法未被直接调用而将其移除或重命名。
# 为了防止 "样式显示不出来" (通常是因为 View 没被正确 inflate) 或 Crash，我们需要保护它们。
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static ** inflate(android.view.LayoutInflater);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static ** bind(android.view.View);
    public ** getRoot();
}

# ----------------------------------------------------------------------------
# 4. Fusion 核心组件保护 (Fusion Core Protection)
# ----------------------------------------------------------------------------
# 保护 JavaDelegate 及其子类。
# 你提到在 Java 代码中直接 Crash，通常是因为 Java 继承 Kotlin 的抽象类时，
# R8 可能会激进地重命名桥接方法或者由于缺少 Metadata 导致签名不匹配。
-keep public class * extends com.fusion.adapter.delegate.JavaDelegate {
    # 保持构造函数，防止反射实例化失败（虽然 Fusion 主要用实例注册，但防范反射创建）
    <init>(...);
    # 保护重写的方法不被混淆，确保多态调用正确
    void onBind(...);
    androidx.viewbinding.ViewBinding onCreateBinding(...);
}

# 保护 FusionDelegate 的基础契约
-keep public class * extends com.fusion.adapter.delegate.FusionDelegate {
    <init>(...);
}

# ----------------------------------------------------------------------------
# 5. 资源与反射 (Resources & Reflection)
# ----------------------------------------------------------------------------
# Fusion 内部使用了 getItem 和 tag (R.id.fusion_item_tag)。
# 虽然 consumer-rules 不能直接控制资源压缩 (shrinkResources)，
# 但我们要确保 Fusion 内部使用的资源 ID 字段名不被混淆，防止反射查找 ID 失败。
-keepclassmembers class **.R$id {
    public static int fusion_item_tag;
}

# ----------------------------------------------------------------------------
# 6. 防范性规则 (Precautionary Rules)
# ----------------------------------------------------------------------------
# 如果用户使用了 FusionPagingAdapter，确保 Paging 相关的类不会因为被 Fusion 引用而产生误报警告
-dontwarn com.fusion.adapter.**