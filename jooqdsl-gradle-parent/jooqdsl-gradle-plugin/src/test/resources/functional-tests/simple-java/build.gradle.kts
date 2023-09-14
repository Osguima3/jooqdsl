import io.github.osguima3.jooqdsl.LatestArtifactVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.21"

    java
    kotlin("jvm") version kotlinVersion
    id("io.github.osguima3.jooqdsl")
}

group = "io.github.osguima3.jooqdsl.simple-java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
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

    register<LatestArtifactVersion>("version") {
        coordinates.set("io.github.osguima3.jooqdsl:jooqdsl-gradle-plugin:1.2.3")
        serverUrl.set("https://potato.com")
    }
}
