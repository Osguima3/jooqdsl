import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.21"

    java
    kotlin("jvm") version kotlinVersion
}

group = "io.github.osguima3.jooqdsl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
        testImplementation("commons-io:commons-io:2.11.0")

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
            }
        }
    }
}
