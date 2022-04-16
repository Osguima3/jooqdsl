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

package io.osguima3.jooqdsl.model

import io.osguima3.jooqdsl.model.context.FieldContext
import io.osguima3.jooqdsl.model.context.TableContext
import io.osguima3.jooqdsl.model.context.converter
import io.osguima3.jooqdsl.model.context.valueObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

typealias ConverterConfig = (FieldContext) -> Unit

class ModelDefinitionTest {

    private val fieldContext = mock<FieldContext>()
    private val tableContext = mock<TableContext> {
        on { it.field(any(), any<ConverterConfig>()) }.then { it.getArgument<ConverterConfig>(1)(fieldContext) }
    }

    private val context = TestModelContext { tableContext }

    @Nested
    inner class `field(name, type)` {

        @Test
        fun `should forward to table context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", String::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(tableContext).run { field("field", String::class) }
        }
    }

    @Nested
    inner class `field(name) { block }` {

        @Test
        fun `should forward type to field context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { type(String::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(fieldContext).type(String::class)
        }

        @Test
        fun `should forward enum to field context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { enum(TestEnum::class, "String") }
                    }
                }
            }

            context.run(definition.configure)

            verify(fieldContext).enum(TestEnum::class, "String")
        }

        @Test
        fun `should forward reified valueObject to field context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { valueObject(TestConverter::class, TestValueObject::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(fieldContext).valueObject(TestConverter::class, TestValueObject::class, Int::class, String::class)
        }

        @Test
        fun `should forward jooq converter to field context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { converter(TestJooqConverter::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(fieldContext).converter(TestJooqConverter::class)
        }

        @Test
        fun `should forward reified converter to field context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { converter(TestConverter::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(fieldContext).converter(TestConverter::class, Int::class, String::class)
        }

        @Test
        fun `should forward custom string to field context`() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { custom(String::class, "custom") }
                    }
                }
            }

            context.run(definition.configure)

            verify(fieldContext).custom(String::class, "custom")
        }
    }
}
