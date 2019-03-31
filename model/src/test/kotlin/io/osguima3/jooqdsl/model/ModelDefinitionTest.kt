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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.osguima3.jooqdsl.model.context.FieldContext
import io.osguima3.jooqdsl.model.context.TableContext
import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.model.context.valueObject
import io.osguima3.jooqdsl.model.converter.Converter
import org.junit.jupiter.api.Test

typealias ConverterConfig = (FieldContext) -> Unit

class ModelDefinitionTest {

    data class IntValueObject(val value: Int)
    data class StringValueObject(val value: String)
    enum class TestEnum
    abstract class IntStringConverter : Converter<Int, String>

    private val converterContext = mock<FieldContext>()
    private val tableContext = mock<TableContext>().apply {
        whenever(field(eq("field"), any<ConverterConfig>()))
            .then { it.getArgument<ConverterConfig>(1).invoke(converterContext) }
    }

    private val context = TestModelContext { tableContext }

    @Test
    fun testField() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field", IntValueObject::class)
                }
            }
        }

        context.run(definition.configure)

        verify(tableContext).run { field("field", IntValueObject::class) }
    }

    @Test
    fun testBlock_Type() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { type(IntValueObject::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).type(IntValueObject::class)
    }

    @Test
    fun testBlock_Enum() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { enum(TestEnum::class, "String") }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).enum(TestEnum::class, "String")
    }

    @Test
    fun testBlock_ValueObject() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { valueObject(IntStringConverter::class, StringValueObject::class, Int::class, String::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).valueObject(IntStringConverter::class, StringValueObject::class, Int::class, String::class)
    }

    @Test
    fun testBlock_ValueObjectReified() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { valueObject(IntStringConverter::class, StringValueObject::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).valueObject(IntStringConverter::class, StringValueObject::class, Int::class, String::class)
    }

    @Test
    fun testBlock_Custom() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { custom(IntStringConverter::class, String::class, Int::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).custom(IntStringConverter::class, String::class, Int::class)
    }

    @Test
    fun testBlock_CustomReified() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { custom(IntStringConverter::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).custom(IntStringConverter::class, String::class, Int::class)
    }
}
