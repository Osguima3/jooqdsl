package org.osguima3.jooqdsl.plugin.context

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Answers.RETURNS_DEEP_STUBS
import org.osguima3.jooqdsl.model.ModelDefinition
import org.osguima3.jooqdsl.model.context.custom
import org.osguima3.jooqdsl.model.context.tinyType
import org.osguima3.jooqdsl.model.converter.Converter
import java.time.Instant

class ModelContextImplTest {

    data class TestTinyType(val value: Int)
    data class TestInstantTinyType(val value: Instant)
    enum class TestEnum
    abstract class TestConverter : Converter<Int, String>

    private val targetPackage = "org.company.project.package"
    private val targetDirectory = "generated/jooq"
    private val converterPackage = Converter::class.java.`package`.name
    private val forcedTypes = mock<MutableList<ForcedType>>()

    private val configuration = mock<Configuration>(defaultAnswer = RETURNS_DEEP_STUBS).also {
        whenever(it.generator.database.forcedTypes).thenReturn(forcedTypes)
        whenever(it.generator.target.directory).thenReturn(targetDirectory)
        whenever(it.generator.target.packageName).thenReturn(targetPackage)
    }

    private val generatedConverters: MutableSet<String> = mock()
    private val context = ModelContextImpl(configuration, generatedConverters)

    @Nested
    inner class Simple {

        @Test
        fun testPrimitive() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", Int::class)
                    }
                }
            }

            context.run(definition.configure)

            verifyZeroInteractions(forcedTypes, generatedConverters)
        }

        @Test
        fun testBoxedPrimitive() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", Integer::class)
                    }
                }
            }

            context.run(definition.configure)

            verifyZeroInteractions(forcedTypes, generatedConverters)
        }

        @Test
        fun testString() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", String::class)
                    }
                }
            }

            context.run(definition.configure)

            verifyZeroInteractions(forcedTypes, generatedConverters)
        }

        @Test
        fun testInstant() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", Instant::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = Instant::class.qualifiedName
                it.converter = "$converterPackage.InstantConverter"
            })
            verify(generatedConverters).add("InstantConverter")
        }

        @Test
        fun testEnum() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", TestEnum::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = TestEnum::class.qualifiedName
                it.converter = "new org.jooq.impl.EnumConverter<>($targetPackage.enums.TestEnum.class, TestEnum.class)"
            })
            verifyZeroInteractions(generatedConverters)
        }

        @Test
        fun testTinyType() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", TestTinyType::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = TestTinyType::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(int.class, TestTinyType.class, " +
                        "TestTinyType::new, TestTinyType::getValue)"
            })
            verifyZeroInteractions(generatedConverters)
        }

        @Test
        fun testInstantTinyType() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", TestInstantTinyType::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = TestInstantTinyType::class.qualifiedName
                it.converter = "new $converterPackage.TinyTypeConverter<>(" +
                        "new $converterPackage.InstantConverter(), " +
                        "TestInstantTinyType::new, TestInstantTinyType::getValue, " +
                        "java.time.OffsetDateTime.class, TestInstantTinyType.class)"
            })
            verify(generatedConverters).add("InstantConverter")
            verify(generatedConverters).add("TinyTypeConverter")
        }
    }

    @Nested
    inner class Custom {

        @Test
        fun testStringEnum() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { enum("string", TestEnum::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = TestEnum::class.qualifiedName
                it.converter = "new org.jooq.impl.EnumConverter<>(string.class, TestEnum.class)"
            })
            verifyZeroInteractions(generatedConverters)
        }

        @Test
        fun testTinyType() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { tinyType(TestTinyType::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = TestTinyType::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(int.class, TestTinyType.class, " +
                        "TestTinyType::new, TestTinyType::getValue)"
            })
            verifyZeroInteractions(generatedConverters)
        }

        @Test
        fun testTinyTypeConverter() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { tinyType(TestConverter::class, TestInstantTinyType::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = TestInstantTinyType::class.qualifiedName
                it.converter = "new $converterPackage.TinyTypeConverter<>(" +
                        "new ${TestConverter::class.qualifiedName}(), " +
                        "TestInstantTinyType::new, TestInstantTinyType::getValue, " +
                        "kotlin.Int.class, TestInstantTinyType.class)"
            })
            verify(generatedConverters).add("TinyTypeConverter")
        }

        @Test
        fun testCustom() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { custom(TestConverter::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = ".*\\.table\\.field"
                it.userType = String::class.qualifiedName
                it.converter = "new $converterPackage.SimpleConverter<>(${TestConverter::class.qualifiedName}, " +
                        "kotlin.Int.class, String.class)"
            })
            verify(generatedConverters).add("SimpleConverter")
        }
    }
}
