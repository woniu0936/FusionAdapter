import kotlinx.validation.ApiValidationExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.binary.compatibility.validator)
}

// 配置验证器
apiValidation {
    // 忽略不需要检查的模块 (比如 app 是示例工程，不需要检查 API)
    // 同时也忽略 build-logic，因为它是构建逻辑
    ignoredProjects.addAll(listOf("app"))

    // 如果有不需要检查的包名，可以在这里配置
    // nonPublicMarkers.add("com.fusion.adapter.core.internal.InternalApi")
}
