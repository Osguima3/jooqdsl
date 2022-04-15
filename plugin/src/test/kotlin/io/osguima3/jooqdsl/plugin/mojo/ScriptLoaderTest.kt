package io.osguima3.jooqdsl.plugin.mojo

import io.osguima3.jooqdsl.model.ModelDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
}
