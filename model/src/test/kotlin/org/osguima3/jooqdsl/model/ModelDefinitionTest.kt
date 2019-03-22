/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

package org.osguima3.jooqdsl.model

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.osguima3.jooqdsl.model.context.FieldContext
import org.osguima3.jooqdsl.model.context.TableContext
import org.osguima3.jooqdsl.model.context.custom
import org.osguima3.jooqdsl.model.context.tinyType
import org.osguima3.jooqdsl.model.converter.Converter

typealias ConverterConfig = (FieldContext) -> Unit

class ModelDefinitionTest {

    data class TestTinyType(val value: Int)
    data class TestStringTinyType(val value: String)
    enum class TestEnum
    abstract class TestConverter : Converter<Int, String>

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
                    field("field", TestTinyType::class)
                }
            }
        }

        context.run(definition.configure)

        verify(tableContext).run { field("field", TestTinyType::class) }
    }

    @Test
    fun testBlock_Type() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { type(TestTinyType::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).type(TestTinyType::class)
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
    fun testBlock_TinyType() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { tinyType(TestConverter::class, TestStringTinyType::class, Int::class, String::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).tinyType(TestConverter::class, TestStringTinyType::class, Int::class, String::class)
    }

    @Test
    fun testBlock_TinyTypeReified() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { tinyType(TestConverter::class, TestStringTinyType::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).tinyType(TestConverter::class, TestStringTinyType::class, Int::class, String::class)
    }

    @Test
    fun testBlock_Custom() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { custom(TestConverter::class, String::class, Int::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).custom(TestConverter::class, String::class, Int::class)
    }

    @Test
    fun testBlock_CustomReified() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { custom(TestConverter::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).custom(TestConverter::class, String::class, Int::class)
    }
}
