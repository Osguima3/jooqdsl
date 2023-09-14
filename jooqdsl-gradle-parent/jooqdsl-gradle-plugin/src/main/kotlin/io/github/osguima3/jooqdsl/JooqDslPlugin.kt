package io.github.osguima3.jooqdsl

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

class JooqDslPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("greeting", GreetingPluginExtension::class.java)

        project.task("hello").doLast {
            println("Hello, ${extension.greeter.getOrElse("LTT")}")
            println("I have a message for You: ${extension.message.getOrElse("From our sponsor!")}")
        }
    }
}

abstract class GreetingPluginExtension {

    @get:Input
    abstract val greeter: Property<String?>

    @get:Input
    abstract val message: Property<String?>
}
