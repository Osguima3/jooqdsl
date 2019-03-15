package org.osguima3.jooqdsl.model

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.osguima3.jooqdsl.model.converter.Converter
import org.osguima3.jooqdsl.model.definition.ConverterContext
import org.osguima3.jooqdsl.model.definition.TableContext
import org.osguima3.jooqdsl.model.definition.TestDefinitionVisitor
import org.osguima3.jooqdsl.model.definition.withCustomConverter
import java.time.Instant

class ModelDefinitionTest {

    data class TestTinyType(val value: Int)
    data class TestInstantTinyType(val value: Instant)
    enum class TestEnum
    abstract class TestConverter : Converter<Int, String>

    private val converterContext = mock<ConverterContext<Int, String>>()

    private val tableContext = mock<TableContext>()

    private val visitor = TestDefinitionVisitor { tableContext }

    @Test
    fun testTinyType() {
        val definition = ModelDefinition {
            tables {
                table("foo") {
                    "tiny_field" withTinyType TestTinyType::class
                }
            }
        }

        definition.accept(visitor)

        verify(tableContext).run { "tiny_field" withTinyType TestTinyType::class }
    }

    @Test
    fun testInstantTinyType() {
        val definition = ModelDefinition {
            tables {
                table("foo") {
                    "instant_field" withInstantTinyType TestInstantTinyType::class
                }
            }
        }

        definition.accept(visitor)

        verify(tableContext).run { "instant_field" withInstantTinyType TestInstantTinyType::class }
    }

    @Test
    fun testEnum() {
        val definition = ModelDefinition {
            tables {
                table("foo") {
                    "enum_field" withEnum TestEnum::class
                }
            }
        }

        definition.accept(visitor)

        verify(tableContext).run { "enum_field" withEnum TestEnum::class }
    }

    @Test
    fun testStringEnum() {
        val definition = ModelDefinition {
            tables {
                table("foo") {
                    "string_enum_field" withStringEnum TestEnum::class
                }
            }
        }

        definition.accept(visitor)

        verify(tableContext).run { "string_enum_field" withStringEnum TestEnum::class }
    }

    @Test
    fun testCustomReified() {
        val definition = setupCustomConverter("custom_field") {
            withCustomConverter("custom_field", TestConverter::class)
        }

        definition.accept(visitor)

        verifyCustomConverter("custom_field")
    }

    @Test
    fun testCustomInfix() {
        val definition = setupCustomConverter("custom_field") {
            "custom_field" withCustomConverter TestConverter::class from Int::class to String::class
        }

        definition.accept(visitor)

        verifyCustomConverter("custom_field")
    }

    @Test
    fun testCustomInfixReversed() {
        val definition = setupCustomConverter("custom_field") {
            "custom_field" withCustomConverter TestConverter::class to String::class from Int::class
        }

        definition.accept(visitor)

        verifyCustomConverter("custom_field")
    }

    private fun setupCustomConverter(name: String, configure: TableContext.() -> Unit): ModelDefinition {
        whenever(tableContext.run { name.withCustomConverter(TestConverter::class) }).thenReturn(converterContext)
        whenever(converterContext from Int::class).thenReturn(converterContext)
        whenever(converterContext to String::class).thenReturn(converterContext)

        return ModelDefinition {
            tables {
                table("foo", configure)
            }
        }
    }

    private fun verifyCustomConverter(name: String) {
        verify(tableContext).run { name withCustomConverter TestConverter::class }
        verify(converterContext, times(1)).run { from(Int::class) }
        verify(converterContext, times(1)).run { to(String::class) }
    }
}
