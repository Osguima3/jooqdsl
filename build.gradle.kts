import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.maven.plugin.development) apply false
    alias(libs.plugins.central.portal.publisher) apply false
    `maven-publish`
    signing
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        coordinates("io.github.osguima3.jooqdsl", project.name, project.version.toString())

        pom {
            name.set(project.name)
            description.set("Type-safe extension to jOOQ's generator plugin")
            url.set("https://github.com/Osguima3/jooqdsl")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    name.set("Oscar Guillén")
                    email.set("osguima3@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:git@github.com:Osguima3/jooqdsl.git")
                developerConnection.set("scm:git:git@github.com:Osguima3/jooqdsl.git")
                url.set("https://github.com/Osguima3/jooqdsl")
            }
        }

        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

        // Only sign when publishing to Maven Central (when credentials are available)
        if (project.hasProperty("signingInMemoryKey") || System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null) {
            signAllPublications()
        }
    }
}
