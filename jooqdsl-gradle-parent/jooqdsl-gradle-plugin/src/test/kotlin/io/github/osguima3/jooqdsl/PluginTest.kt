package io.github.osguima3.jooqdsl

import org.apache.commons.io.FileUtils
import org.gradle.internal.impldep.junit.framework.TestCase.assertNotNull
import org.gradle.internal.impldep.junit.framework.TestCase.assertTrue
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PluginTest {

    @TempDir
    private lateinit var tempDir: File

    @Test
    fun greetingTest() {
        with(ProjectBuilder.builder().build()) {
            pluginManager.apply("io.github.osguima3.jooqdsl")
            tasks.register("version", LatestArtifactVersion::class.java)
            assertTrue(pluginManager.hasPlugin("io.github.osguima3.jooqdsl"))
            assertNotNull(tasks.getByName("hello"))
            assertNotNull(tasks.getByName("version"))
        }
    }

    @Test
    fun `should run the plugin`() {
        val resourcesDir = File("src/test/resources/functional-tests/simple-java")
        FileUtils.copyDirectory(resourcesDir, tempDir)

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("hello", "version")
            .withPluginClasspath()
            .build()

        println(result.output)
        assertTrue(result.output.contains("I have a message for You: From our sponsor!"))
        assertTrue(result.task(":hello")?.outcome == TaskOutcome.SUCCESS)
        assertTrue(result.task(":version")?.outcome == TaskOutcome.SUCCESS)
    }
}
