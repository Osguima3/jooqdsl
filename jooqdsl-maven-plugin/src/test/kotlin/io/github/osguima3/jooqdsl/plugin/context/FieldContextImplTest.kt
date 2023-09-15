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

package io.github.osguima3.jooqdsl.plugin.context

import io.github.osguima3.jooqdsl.model.context.converter
import io.github.osguima3.jooqdsl.model.context.valueObject
import io.github.osguima3.jooqdsl.plugin.converter.CompositeDefinition
import io.github.osguima3.jooqdsl.plugin.converter.CustomConverterDefinition
import io.github.osguima3.jooqdsl.plugin.converter.EnumDefinition
import io.github.osguima3.jooqdsl.plugin.converter.InstantConverterDefinition
import io.github.osguima3.jooqdsl.plugin.converter.SimpleConverterDefinition
import io.github.osguima3.jooqdsl.plugin.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.plugin.converter.ValueObjectDefinition
import io.github.osguima3.jooqdsl.plugin.types.JavaConverter
import io.github.osguima3.jooqdsl.plugin.types.JavaInvalidConverter
import io.github.osguima3.jooqdsl.plugin.types.JavaUnsupportedObject
import io.github.osguima3.jooqdsl.plugin.types.JavaValueObject
import io.github.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.github.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.github.osguima3.jooqdsl.plugin.types.KotlinInstantValueObject
import io.github.osguima3.jooqdsl.plugin.types.KotlinInvalidConverter
import io.github.osguima3.jooqdsl.plugin.types.KotlinJooqConverter
import io.github.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
import io.github.osguima3.jooqdsl.plugin.types.KotlinUnsupportedObject
import io.github.osguima3.jooqdsl.plugin.types.KotlinUnsupportedValueObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class FieldContextImplTest {

    private val targetPackage = "io.osguima3.project.package"

    private val jooqContext = mock<JooqContext> {
        on { it.targetPackage } doReturn targetPackage
    }

    private val context = FieldContextImpl(jooqContext.targetPackage)

    @Nested
    inner class `Type (native)` {

        @Test
        fun `should skip primitive types`() {
            val definition = context.type(Int::class)

            assertEquals(SkippedDefinition, definition)
        }

        @Test
        fun `should skip boxed primitive types`() {
            val definition = context.type(Integer::class)

            assertEquals(SkippedDefinition, definition)
        }

        @Test
        fun `should skip BigDecimal type`() {
            val definition = context.type(BigDecimal::class)

            assertEquals(SkippedDefinition, definition)
        }

        @Test
        fun `should skip String type`() {
            val definition = context.type(String::class)

            assertEquals(SkippedDefinition, definition)
        }

        @Test
        fun `should skip UUID type`() {
            val definition = context.type(UUID::class)

            assertEquals(SkippedDefinition, definition)
        }

        @Test
        fun `should correctly register Instant type`() {
            val definition = context.type(Instant::class)

            assertEquals(InstantConverterDefinition, definition)
        }
    }

    @Nested
    inner class TypeJava {

        @Test
        fun `should throw IllegalArgumentException for java value object types`() {
            assertThrows<IllegalArgumentException> {
                context.type(JavaValueObject::class)
            }
        }

        @Test
        fun `should throw IllegalArgumentException if no mapper is available`() {
            assertThrows<IllegalArgumentException> {
                context.type(JavaUnsupportedObject::class)
            }
        }
    }

    @Nested
    inner class TypeKotlin {

        @Test
        fun `should correctly register enum types`() {
            val definition = context.type(KotlinEnum::class)

            assertEquals(EnumDefinition("$targetPackage.enums.KotlinEnum", KotlinEnum::class), definition)
        }

        @Test
        fun `should correctly register simple value object types`() {
            val definition = context.type(KotlinStringValueObject::class)

            assertEquals(ValueObjectDefinition(KotlinStringValueObject::class), definition)
        }

        @Test
        fun `should correctly register Instant value object type`() {
            val definition = context.type(KotlinInstantValueObject::class)

            assertEquals(
                CompositeDefinition(InstantConverterDefinition, ValueObjectDefinition(KotlinInstantValueObject::class)),
                definition
            )
        }

        @Test
        fun `should throw IllegalArgumentException if no mapper is available`() {
            assertThrows<IllegalArgumentException> {
                context.type(KotlinUnsupportedObject::class)
            }
        }

        @Test
        fun `should throw IllegalArgumentException if no value object mapper is available`() {
            assertThrows<IllegalArgumentException> {
                context.type(KotlinUnsupportedValueObject::class)
            }
        }
    }

    @Nested
    inner class Enum {

        @Test
        fun `should correctly register enum type with custom database type`() {
            val definition = context.enum(KotlinEnum::class, "String")

            assertEquals(EnumDefinition("String", KotlinEnum::class), definition)
        }
    }

    @Nested
    inner class ValueObject {

        @Test
        fun `should correctly register kotlin value object type with custom converter`() {
            val definition = context.valueObject(KotlinConverter::class, KotlinStringValueObject::class)

            assertEquals(
                CompositeDefinition(
                    SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class),
                    ValueObjectDefinition(KotlinStringValueObject::class)
                ),
                definition
            )
        }

        @Test
        fun `should throw IllegalArgumentException if type is not a value type`() {
            assertThrows<IllegalArgumentException> {
                context.valueObject(JavaConverter::class, String::class)
            }
        }

        @Test
        fun `should throw IllegalArgumentException for java value object types (not supported)`() {
            assertThrows<IllegalArgumentException> {
                context.valueObject(JavaConverter::class, JavaValueObject::class)
            }
        }

        @Test
        fun `should throw IllegalArgumentException if value field type does not match converter from type`() {
            assertThrows<IllegalArgumentException> {
                context.valueObject(KotlinConverter::class, KotlinInstantValueObject::class)
            }
        }
    }

    @Nested
    inner class Converter {

        @Test
        fun `should correctly register Kotlin converter`() {
            val definition = context.converter(KotlinConverter::class)

            assertEquals(SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class), definition)
        }

        @Test
        fun `should throw IllegalArgumentException if Kotlin converter is not an object`() {
            assertThrows<IllegalArgumentException> {
                context.converter(KotlinInvalidConverter::class)
            }
        }

        @Test
        fun `should correctly register Java converter`() {
            val definition = context.converter(JavaConverter::class)

            assertEquals(SimpleConverterDefinition(Int::class, String::class, JavaConverter::class), definition)
        }

        @Test
        fun `should throw IllegalArgumentException if Java converter does not have an INSTANCE singleton field`() {
            assertThrows<IllegalArgumentException> {
                context.converter(JavaInvalidConverter::class)
            }
        }
    }

    @Nested
    inner class JooqConverter {

        @Test
        fun `should correctly register jOOQ converter`() {
            val definition = context.converter(KotlinJooqConverter::class)

            assertEquals(CustomConverterDefinition(KotlinJooqConverter::class, String::class), definition)
        }
    }

    @Nested
    inner class Custom {

        @Test
        fun `should correctly register custom converter`() {
            val definition = context.custom(Int::class, "custom")

            assertEquals(CustomConverterDefinition("custom", Int::class), definition)
        }
    }
}
