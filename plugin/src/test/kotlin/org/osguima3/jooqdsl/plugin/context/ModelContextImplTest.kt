package org.osguima3.jooqdsl.plugin.context

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Answers.RETURNS_DEEP_STUBS
import org.osguima3.jooqdsl.model.ModelDefinition
import org.osguima3.jooqdsl.model.context.custom
import org.osguima3.jooqdsl.model.context.tinyType
import org.osguima3.jooqdsl.model.converter.Converter
import org.osguima3.jooqdsl.plugin.context.TemplateFile.SIMPLE
import org.osguima3.jooqdsl.plugin.context.TemplateFile.TINY_TYPE
import java.time.Instant

class ModelContextImplTest {

    data class TestTinyType(val value: Int)
    data class TestInstantTinyType(val value: Instant)
    enum class TestEnum
    abstract class TestConverter : Converter<Int, String>

    private val expression = ".*\\.table\\.field"
    private val targetDirectory = "generated/jooq"
    private val targetPackage = "org.company.project.package"
    private val converterPackage = "$targetPackage.converters"
    private val forcedTypes: MutableList<ForcedType> = mock()

    private val configuration = mock<Configuration>(defaultAnswer = RETURNS_DEEP_STUBS).also {
        whenever(it.generator.target.directory).thenReturn(targetDirectory)
        whenever(it.generator.target.packageName).thenReturn(targetPackage)
        whenever(it.generator.database.forcedTypes).thenReturn(forcedTypes)
    }

    private val context: ModelContextImpl = ModelContextImpl(configuration)

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

            verifyZeroInteractions(forcedTypes)
            assertThat(context.pendingTemplates).isEmpty()
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

            verifyZeroInteractions(forcedTypes)
            assertThat(context.pendingTemplates).isEmpty()
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

            verifyZeroInteractions(forcedTypes)
            assertThat(context.pendingTemplates).isEmpty()
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
                it.expression = expression
                it.userType = Instant::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.time.OffsetDateTime.class, java.time.Instant.class, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC))"
            })
            assertThat(context.pendingTemplates).isEmpty()
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
                it.expression = expression
                it.userType = TestEnum::class.qualifiedName
                it.converter = "new org.jooq.impl.EnumConverter<>($targetPackage.enums.TestEnum.class, TestEnum.class)"
            })
            assertThat(context.pendingTemplates).isEmpty()
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
                it.expression = expression
                it.userType = TestTinyType::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(int.class, TestTinyType.class, " +
                    "TestTinyType::new, TestTinyType::getValue)"
            })
            assertThat(context.pendingTemplates).isEmpty()
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
                it.expression = expression
                it.userType = TestInstantTinyType::class.qualifiedName
                it.converter = "new $converterPackage.TinyTypeConverter<>(" +
                    "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, java.time.Instant.class, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)), " +
                    "TestInstantTinyType::new, TestInstantTinyType::getValue, " +
                    "java.time.OffsetDateTime.class, TestInstantTinyType.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(SIMPLE, TINY_TYPE)
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
                it.expression = expression
                it.userType = TestEnum::class.qualifiedName
                it.converter = "new org.jooq.impl.EnumConverter<>(string.class, TestEnum.class)"
            })
            assertThat(context.pendingTemplates).isEmpty()
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
                it.expression = expression
                it.userType = TestTinyType::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(int.class, TestTinyType.class, " +
                    "TestTinyType::new, TestTinyType::getValue)"
            })
            assertThat(context.pendingTemplates).isEmpty()
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
                it.expression = expression
                it.userType = TestInstantTinyType::class.qualifiedName
                it.converter = "new $converterPackage.TinyTypeConverter<>(" +
                    "new ${TestConverter::class.qualifiedName}(), " +
                    "TestInstantTinyType::new, TestInstantTinyType::getValue, " +
                    "kotlin.Int.class, TestInstantTinyType.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(TINY_TYPE)
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
                it.expression = expression
                it.userType = String::class.qualifiedName
                it.converter = "new $converterPackage.SimpleConverter<>(new ${TestConverter::class.qualifiedName}(), " +
                    "kotlin.Int.class, String.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(SIMPLE)
        }
    }
}
