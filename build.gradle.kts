import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.maven.plugin.development) apply false
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
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

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

    val sourcesJar by tasks.registering(Jar::class) {
        from(project.the<SourceSetContainer>()["main"].allSource)
        archiveClassifier.set("sources")
    }

    val dokkaJar by tasks.registering(Jar::class) {
        dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
        from(tasks.named("dokkaGeneratePublicationHtml").get().outputs)
        archiveClassifier.set("javadoc")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(sourcesJar)
                artifact(dokkaJar)

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
            }
        }

        repositories {
            maven {
                name = "central"
                url = uri("https://central.sonatype.com/api/v1/publisher")
                credentials {
                    username = System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatypeUsername") as String?
                    password = System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatypePassword") as String?
                }
            }
        }
    }

    configure<SigningExtension> {
        val signingKey = System.getenv("GPG_SIGNING_KEY") ?: findProperty("signing.key") as String?
        val signingPassword = System.getenv("GPG_SIGNING_PASSWORD") ?: findProperty("signing.password") as String?

        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }

        sign(the<PublishingExtension>().publications["maven"])
    }

    // Only sign when in release mode
    tasks.withType<Sign> {
        onlyIf { project.hasProperty("release") || System.getenv("RELEASE") == "true" }
    }
}
