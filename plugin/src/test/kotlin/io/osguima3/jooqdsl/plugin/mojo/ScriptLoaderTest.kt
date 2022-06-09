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
            this::class.java.getResource("/mojo/model_definition.kts")!!.openStream().reader().use {
                val modelDefinition = fileLoader.loadScript<ModelDefinition>(it)
                assertThat(modelDefinition.configure).isNotNull
            }
        }
    }
}
