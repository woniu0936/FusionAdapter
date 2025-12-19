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

// ============================================================================
// Git Hook Installation
// ============================================================================
// ============================================================================
// Git Hook Installation
// ============================================================================
tasks.register<Copy>("installGitHooks") {
    description = "Installs git hooks from config/git-hooks to .git/hooks"
    group = "git hooks"

    from(layout.projectDirectory.dir("config/git-hooks"))
    into(layout.projectDirectory.dir(".git/hooks"))

    // 使用 Gradle 8.x 新的权限 API
    filePermissions {
        unix("777") // 赋予所有读写执行权限
    }

    doLast {
        // 只有当文件真正被复制或更新时，才会打印（利用 Copy 任务的缓存特性）
        println("🪝  Git pre-commit hook installed successfully.")
    }
}

if (tasks.findByName("clean") == null) {
    tasks.register<Delete>("clean") {
        delete(rootProject.layout.buildDirectory)
    }
}

// 挂载到 clean 任务 (命令行兜底)
tasks.named("clean") {
    dependsOn("installGitHooks")
}

// 尝试挂载到 IDE Sync 任务 (最佳体验)
try {
    tasks.named("prepareKotlinBuildScriptModel") {
        dependsOn("installGitHooks")
    }
} catch (e: UnknownTaskException) {
    // 忽略异常 (在纯命令行或非 AS 环境下该任务可能不存在)
}
