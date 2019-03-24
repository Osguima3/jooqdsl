package org.osguima3.jooqdsl.plugin.mojo

import org.apache.maven.plugin.MojoExecutionException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.osguima3.jooqdsl.model.ModelDefinition
import java.io.Reader

class ScriptLoaderTest {

    private val fileLoader = ScriptLoader()

    @Nested
    inner class LoadScript {

        @Test
        fun `should successfully load script`() {
            val resource = this::class.java.getResource("/mojo/model_definition.kts")
            val modelDefinition: ModelDefinition = fileLoader.loadScript(resource.openStream().reader())
            assertThat(modelDefinition.configure).isNotNull
        }
    }

    @Nested
    inner class ReadFile {

        @Test
        @Disabled
        fun `should read file if it exists`() {
            val resource = this::class.java.getResource("/mojo/dummy.txt")
            val readFile: Reader = fileLoader.readFile(resource.path)
            assertThat(readFile.readText()).isEqualTo("File content")
        }

        @Test
        fun `should fail when file does not exist`() {
            assertThrows<MojoExecutionException> {
                fileLoader.readFile("unknown")
            }
        }
    }
}
