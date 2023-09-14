package io.github.osguima3.jooqdsl

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class LatestArtifactVersion : DefaultTask() {

    @get:Input
    abstract val coordinates: Property<String>

    @get:Input
    abstract val serverUrl: Property<String>

    @TaskAction
    fun resolveLatestVersion() {
        println("""Retrieving artifact ${coordinates.get()} from ${serverUrl.get()}""")
    }
}
