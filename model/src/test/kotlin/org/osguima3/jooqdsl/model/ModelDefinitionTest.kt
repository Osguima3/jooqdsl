package org.osguima3.jooqdsl.model

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.osguima3.jooqdsl.model.context.*
import org.osguima3.jooqdsl.model.converter.Converter

typealias ConverterConfig = (FieldContext) -> FieldDefinition<Int>

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
    fun testCustom_Enum() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { enum("String", TestEnum::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).enum("String", TestEnum::class)
    }

    @Test
    fun testCustom_TinyType() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { tinyType(TestTinyType::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).tinyType(TestTinyType::class)
    }

    @Test
    fun testCustom_TinyTypeConverter() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { tinyType(TestConverter::class, Int::class, TestStringTinyType::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).tinyType(TestConverter::class, Int::class, TestStringTinyType::class)
    }

    @Test
    fun testCustom_TinyTypeConverterReified() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { tinyType(TestConverter::class, TestStringTinyType::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).tinyType(TestConverter::class, Int::class, TestStringTinyType::class)
    }

    @Test
    fun testField_CustomConverter() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { custom(TestConverter::class, Int::class, String::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).custom(TestConverter::class, Int::class, String::class)
    }

    @Test
    fun testField_CustomConverterReified() {
        val definition = ModelDefinition {
            tables {
                table("table") {
                    field("field") { custom(TestConverter::class) }
                }
            }
        }

        context.run(definition.configure)

        verify(converterContext).custom(TestConverter::class, Int::class, String::class)
    }

//    @Test
//    fun testCustom_CustomFromTo() {
//        val definition = ModelDefinition {
//            tables {
//                table("table") {
//                    field("field") { custom(Int::toString, Integer::valueOf, Int::class, String::class) }
//                }
//            }
//        }
//
//        context.run(definition.configure)
//
//        verify(converterContext).custom(Int::toString, Integer::valueOf)
//    }
//
//    @Test
//    fun testField_CustomFromToReified() {
//        val definition = ModelDefinition {
//            tables {
//                table("table") {
//                    field("field") { custom(Int::toString, Integer::valueOf) }
//                }
//            }
//        }
//
//        context.run(definition.configure)
//
//        verify(converterContext).custom(Int::toString, Integer::valueOf)
//    }
}
