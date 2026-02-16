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

package io.github.osguima3.jooqdsl.core.context

import io.github.osguima3.jooqdsl.core.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.model.ModelDefinition
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ModelContextImplTest {

    private val jooqContext = mock<JooqContext>()

    private val context = ModelContextImpl(jooqContext)

    @Test
    fun `should call context from field(name, type)`() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field", String::class)
                }
            }
        }

        definition.configure(context)

        verify(jooqContext).configureField("table", "field", SkippedDefinition)
    }

    @Test
    fun `should call context from field(name) { block }`() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { type(String::class) }
                }
            }
        }

        definition.configure(context)

        verify(jooqContext).configureField("table", "field", SkippedDefinition)
    }

    @Test
    fun `should fail if tables block is duplicated`() {
        val definition = ModelDefinition {
            tables {}
            tables {}
        }

        val e = assertThrows<IllegalArgumentException> {
            definition.configure(context)
        }

        assertThat(e.message).isEqualTo("Tables block already declared")
    }

    @Test
    fun `should fail if table name is duplicated`() {
        val definition = ModelDefinition {
            tables {
                table("table") {}
                table("table") {}
            }
        }

        val e = assertThrows<IllegalArgumentException> {
            definition.configure(context)
        }

        assertThat(e.message).isEqualTo("Table 'table' already declared")
    }

    @Test
    fun `should fail if field name is duplicated`() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") {}
                    field("field") {}
                }
            }
        }

        val e = assertThrows<IllegalArgumentException> {
            definition.configure(context)
        }

        assertThat(e.message).isEqualTo("Field 'table.field' already declared")
    }
}
