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

package io.osguima3.jooqdsl.plugin.context

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
import io.osguima3.jooqdsl.model.ModelDefinition
import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.model.context.tinyType
import io.osguima3.jooqdsl.model.converter.Converter
import io.osguima3.jooqdsl.plugin.converter.TemplateFile.ADAPTER
import io.osguima3.jooqdsl.plugin.converter.TemplateFile.TINY_TYPE
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ModelContextImplTest {

    data class TestIntTinyType(val value: Int)
    data class TestStringTinyType(val value: String)
    data class TestInstantTinyType(val value: Instant)
    enum class TestEnum
    abstract class TestConverter : Converter<Int, String>

    private val expression = ".*\\.table\\.field"
    private val targetDirectory = "generated/jooq"
    private val targetPackage = "io.osguima3.project.package"
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
        fun testBigDecimal() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", BigDecimal::class)
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
        fun testUUID() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", UUID::class)
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
        fun testIntTinyType() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", TestIntTinyType::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = TestIntTinyType::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, TestIntTinyType.class, " +
                    "TestIntTinyType::new, TestIntTinyType::getValue)"
            })
            assertThat(context.pendingTemplates).isEmpty()
        }

        @Test
        fun testStringTinyType() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", TestStringTinyType::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = TestStringTinyType::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(java.lang.String.class, TestStringTinyType.class, " +
                    "TestStringTinyType::new, TestStringTinyType::getValue)"
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
            assertThat(context.pendingTemplates).containsExactly(ADAPTER, TINY_TYPE)
        }
    }

    @Nested
    inner class Custom {

        @Test
        fun testStringEnum() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { enum(TestEnum::class, "String") }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = TestEnum::class.qualifiedName
                it.converter = "new org.jooq.impl.EnumConverter<>(String.class, TestEnum.class)"
            })
            assertThat(context.pendingTemplates).isEmpty()
        }

        @Test
        fun testTinyTypeConverter() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { tinyType(TestConverter::class, TestStringTinyType::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = TestStringTinyType::class.qualifiedName
                it.converter = "new $converterPackage.TinyTypeConverter<>(" +
                    "new ${TestConverter::class.qualifiedName}(), " +
                    "TestStringTinyType::new, TestStringTinyType::getValue, " +
                    "java.lang.Integer.class, TestStringTinyType.class)"
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
                it.converter = "new $converterPackage.ConverterAdapter<>(new ${TestConverter::class.qualifiedName}(), " +
                    "java.lang.Integer.class, String.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(ADAPTER)
        }
    }
}
