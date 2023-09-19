/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 */

package io.github.osguima3.jooqdsl.plugin.mojo

import io.github.osguima3.jooqdsl.model.ModelDefinition
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
