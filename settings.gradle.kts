pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FusionAdapter"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":fusion-core")
include(":fusion-paging")
