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

package io.github.osguima3.jooqdsl.core.context

import io.github.osguima3.jooqdsl.core.converter.CompositeDefinition
import io.github.osguima3.jooqdsl.core.converter.CustomConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.EnumDefinition
import io.github.osguima3.jooqdsl.core.converter.FieldDefinition
import io.github.osguima3.jooqdsl.core.converter.InstantConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.SimpleConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.core.converter.ValueObjectDefinition
import io.github.osguima3.jooqdsl.core.types.JavaConverter
import io.github.osguima3.jooqdsl.core.types.JavaInvalidConverter
import io.github.osguima3.jooqdsl.core.types.JavaRecord
import io.github.osguima3.jooqdsl.core.types.JavaRecordWithMethods
import io.github.osguima3.jooqdsl.core.types.JavaUnsupportedObject
import io.github.osguima3.jooqdsl.core.types.JavaValueObject
import io.github.osguima3.jooqdsl.core.types.JavaValueObjectWithMethods
import io.github.osguima3.jooqdsl.core.types.KotlinConverter
import io.github.osguima3.jooqdsl.core.types.KotlinEnum
import io.github.osguima3.jooqdsl.core.types.KotlinInstantValueObject
import io.github.osguima3.jooqdsl.core.types.KotlinInvalidConverter
import io.github.osguima3.jooqdsl.core.types.KotlinJooqConverter
import io.github.osguima3.jooqdsl.core.types.KotlinStringValueObject
import io.github.osguima3.jooqdsl.core.types.KotlinUnsupportedObject
import io.github.osguima3.jooqdsl.core.types.KotlinUnsupportedValueObject
import io.github.osguima3.jooqdsl.core.types.KotlinValueObjectWithMethods
import io.github.osguima3.jooqdsl.model.context.converter
import io.github.osguima3.jooqdsl.model.context.valueObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class FieldContextImplTest {

    private val tableName = "table"
    private val fieldName = "field"

    private val jooqContext = mock<JooqContext>()

    private val context = FieldContextImpl(jooqContext, tableName, fieldName)

    @Nested
    inner class `Type (native)` {

        @Test
        fun `should skip primitive types`() {
            context.type(Int::class)

            verifyDefinition(SkippedDefinition)
        }

        @Test
        fun `should skip boxed primitive types`() {
            context.type(Integer::class)

            verifyDefinition(SkippedDefinition)
        }

        @Test
        fun `should skip BigDecimal type`() {
            context.type(BigDecimal::class)

            verifyDefinition(SkippedDefinition)
        }

        @Test
        fun `should skip String type`() {
            context.type(String::class)

            verifyDefinition(SkippedDefinition)
        }

        @Test
        fun `should skip UUID type`() {
            context.type(UUID::class)

            verifyDefinition(SkippedDefinition)
        }

        @Test
        fun `should correctly register Instant type`() {
            context.type(Instant::class)

            verifyDefinition(InstantConverterDefinition)
        }
    }

    @Nested
    inner class TypeJava {

        @Test
        fun `should correctly register class value object types`() {
            context.type(JavaValueObject::class)

            verifyDefinition(ValueObjectDefinition(JavaValueObject::class))
        }

        @Test
        fun `should correctly register class value object types with additional methods`() {
            context.type(JavaValueObjectWithMethods::class)

            verifyDefinition(ValueObjectDefinition(JavaValueObjectWithMethods::class))
        }

        @Test
        fun `should correctly register record value object types`() {
            context.type(JavaRecord::class)

            verifyDefinition(ValueObjectDefinition(JavaRecord::class))
        }

        @Test
        fun `should correctly register record value object types with additional methods`() {
            context.type(JavaRecordWithMethods::class)

            verifyDefinition(ValueObjectDefinition(JavaRecordWithMethods::class))
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
            context.type(KotlinEnum::class)

            verifyDefinition(EnumDefinition(KotlinEnum::class))
        }

        @Test
        fun `should correctly register value object types`() {
            context.type(KotlinStringValueObject::class)

            verifyDefinition(ValueObjectDefinition(KotlinStringValueObject::class))
        }

        @Test
        fun `should correctly register value object types with additional methods`() {
            context.type(KotlinValueObjectWithMethods::class)

            verifyDefinition(ValueObjectDefinition(KotlinValueObjectWithMethods::class))
        }

        @Test
        fun `should correctly register Instant value object types`() {
            context.type(KotlinInstantValueObject::class)

            verifyDefinition(
                CompositeDefinition(InstantConverterDefinition, ValueObjectDefinition(KotlinInstantValueObject::class))
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
            context.enum(KotlinEnum::class, "String")

            verifyDefinition(EnumDefinition(KotlinEnum::class, "String"))
        }
    }

    @Nested
    inner class ValueObject {

        @Test
        fun `should correctly register kotlin value object type with custom converter`() {
            context.valueObject(KotlinConverter::class, KotlinStringValueObject::class)

            verifyDefinition(
                CompositeDefinition(
                    SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class),
                    ValueObjectDefinition(KotlinStringValueObject::class)
                )
            )
        }

        @Test
        fun `should correctly register java class value object type with custom converter`() {
            context.valueObject(JavaConverter::class, JavaValueObject::class)

            verifyDefinition(
                CompositeDefinition(
                    SimpleConverterDefinition(Int::class, String::class, JavaConverter::class),
                    ValueObjectDefinition(JavaValueObject::class)
                )
            )
        }

        @Test
        fun `should correctly register java record value object type with custom converter`() {
            context.valueObject(JavaConverter::class, JavaRecord::class)

            verifyDefinition(
                CompositeDefinition(
                    SimpleConverterDefinition(Int::class, String::class, JavaConverter::class),
                    ValueObjectDefinition(JavaRecord::class)
                )
            )
        }

        @Test
        fun `should throw IllegalArgumentException if type is not a value type`() {
            assertThrows<IllegalArgumentException> {
                context.valueObject(JavaConverter::class, String::class)
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
            context.converter(KotlinConverter::class)

            verifyDefinition(SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class))
        }

        @Test
        fun `should throw IllegalArgumentException if Kotlin converter is not an object`() {
            assertThrows<IllegalArgumentException> {
                context.converter(KotlinInvalidConverter::class)
            }
        }

        @Test
        fun `should correctly register Java converter`() {
            context.converter(JavaConverter::class)

            verifyDefinition(SimpleConverterDefinition(Int::class, String::class, JavaConverter::class))
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
            context.converter(KotlinJooqConverter::class)

            verifyDefinition(CustomConverterDefinition(KotlinJooqConverter::class, String::class))
        }
    }

    @Nested
    inner class Custom {

        @Test
        fun `should correctly register custom converter`() {
            context.custom(Int::class, "custom")

            verifyDefinition(CustomConverterDefinition("custom", Int::class))
        }
    }

    fun verifyDefinition(expected: FieldDefinition) = verify(jooqContext).configureField(tableName, fieldName, expected)
}
