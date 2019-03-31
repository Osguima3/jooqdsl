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
import io.osguima3.jooqdsl.model.ModelDefinition
import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.model.context.valueObject
import io.osguima3.jooqdsl.plugin.InstantValueObject
import io.osguima3.jooqdsl.plugin.IntStringConverter
import io.osguima3.jooqdsl.plugin.IntValueObject
import io.osguima3.jooqdsl.plugin.StringValueObject
import io.osguima3.jooqdsl.plugin.TestEnum
import io.osguima3.jooqdsl.plugin.converter.TemplateFile.ADAPTER
import io.osguima3.jooqdsl.plugin.converter.TemplateFile.VALUE_OBJECT
import org.assertj.core.api.Assertions.assertThat
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Answers.RETURNS_DEEP_STUBS
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ModelContextImplTest {

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

    private val context = ModelContextImpl(configuration)

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
        fun IntValueObject() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", IntValueObject::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = IntValueObject::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, IntValueObject.class, " +
                    "IntValueObject::new, IntValueObject::getValue)"
            })
            assertThat(context.pendingTemplates).isEmpty()
        }

        @Test
        fun StringValueObject() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", StringValueObject::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = StringValueObject::class.qualifiedName
                it.converter = "org.jooq.Converter.ofNullable(java.lang.String.class, StringValueObject.class, " +
                    "StringValueObject::new, StringValueObject::getValue)"
            })
            assertThat(context.pendingTemplates).isEmpty()
        }

        @Test
        fun InstantValueObject() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field", InstantValueObject::class)
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = InstantValueObject::class.qualifiedName
                it.converter = "new $converterPackage.ValueObjectConverter<>(" +
                    "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, java.time.Instant.class, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)), " +
                    "InstantValueObject::new, InstantValueObject::getValue, " +
                    "java.time.OffsetDateTime.class, InstantValueObject.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(ADAPTER, VALUE_OBJECT)
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
        fun testValueObjectConverter() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { valueObject(IntStringConverter::class, StringValueObject::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = StringValueObject::class.qualifiedName
                it.converter = "new $converterPackage.ValueObjectConverter<>(" +
                    "new ${IntStringConverter::class.qualifiedName}(), " +
                    "StringValueObject::new, StringValueObject::getValue, " +
                    "java.lang.Integer.class, StringValueObject.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(VALUE_OBJECT)
        }

        @Test
        fun testCustom() {
            val definition = ModelDefinition {
                tables {
                    table("table") {
                        field("field") { custom(IntStringConverter::class) }
                    }
                }
            }

            context.run(definition.configure)

            verify(forcedTypes).add(ForcedType().also {
                it.expression = expression
                it.userType = String::class.qualifiedName
                it.converter = "new $converterPackage.ConverterAdapter<>(new ${IntStringConverter::class.qualifiedName}(), " +
                    "java.lang.Integer.class, String.class)"
            })
            assertThat(context.pendingTemplates).containsExactly(ADAPTER)
        }
    }
}
