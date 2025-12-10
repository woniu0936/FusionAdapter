import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class FusionPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.vanniktech.maven.publish")

            extensions.configure<MavenPublishBaseExtension> {
                val myGroup = providers.gradleProperty("GROUP").getOrElse("com.fusion.adapter")
                val myVersion = providers.gradleProperty("VERSION_NAME").getOrElse("0.0.1")

                coordinates(myGroup, project.name, myVersion)

                pom {
                    name.set(project.name)
                    description.set("A top-tier RecyclerView adapter library for Android.")
                    url.set("https://github.com/woniu0936/FusionAdapter")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("woniu0936")
                            name.set("woniu0936")
                            email.set("woniu0936@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/woniu0936/FusionAdapter.git")
                        developerConnection.set("scm:git:ssh://github.com/woniu0936/FusionAdapter.git")
                        url.set("https://github.com/woniu0936/FusionAdapter/tree/main")
                    }
                }

                // 纯净配置，不需要 automaticRelease 参数，也不需要手动读取 properties
                publishToMavenCentral()
                signAllPublications()
            }
        }
    }
}