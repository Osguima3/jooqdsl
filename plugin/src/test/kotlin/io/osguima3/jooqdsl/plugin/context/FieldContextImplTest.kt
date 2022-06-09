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

import io.osguima3.jooqdsl.model.context.converter
import io.osguima3.jooqdsl.model.context.valueObject
import io.osguima3.jooqdsl.plugin.converter.CompositeDefinition
import io.osguima3.jooqdsl.plugin.converter.ConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.CustomConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.EnumDefinition
import io.osguima3.jooqdsl.plugin.converter.InstantConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.SimpleConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.ValueObjectDefinition
import io.osguima3.jooqdsl.plugin.types.JavaConverter
import io.osguima3.jooqdsl.plugin.types.JavaInvalidConverter
import io.osguima3.jooqdsl.plugin.types.JavaUnsupportedObject
import io.osguima3.jooqdsl.plugin.types.JavaValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.osguima3.jooqdsl.plugin.types.KotlinInstantValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinInvalidConverter
import io.osguima3.jooqdsl.plugin.types.KotlinJooqConverter
import io.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinUnsupportedObject
import io.osguima3.jooqdsl.plugin.types.KotlinUnsupportedValueObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class FieldContextImplTest {

    private val expression = ".*\\.table\\.field"
    private val targetPackage = "io.osguima3.project.package"

    private val jooqContext = mock<JooqContext> {
        on { it.targetPackage } doReturn targetPackage
    }

    private val context = FieldContextImpl(jooqContext, "table", "field")

    @Nested
    inner class `Type (native)` {

        @Test
        fun `should skip primitive types`() {
            context.type(Int::class)

            verifyNoInteractions(jooqContext)
        }

        @Test
        fun `should skip boxed primitive types`() {
            context.type(Integer::class)

            verifyNoInteractions(jooqContext)
        }

        @Test
        fun `should skip BigDecimal type`() {
            context.type(BigDecimal::class)

            verifyNoInteractions(jooqContext)
        }

        @Test
        fun `should skip String type`() {
            context.type(String::class)

            verifyNoInteractions(jooqContext)
        }

        @Test
        fun `should skip UUID type`() {
            context.type(UUID::class)

            verifyNoInteractions(jooqContext)
        }

        @Test
        fun `should correctly register Instant type`() {
            context.type(Instant::class)

            verifyForcedType(InstantConverterDefinition)
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
            context.type(KotlinEnum::class)

            verifyForcedType(EnumDefinition("$targetPackage.enums.KotlinEnum", KotlinEnum::class))
        }

        @Test
        fun `should correctly register simple value object types`() {
            context.type(KotlinStringValueObject::class)

            verifyForcedType(ValueObjectDefinition(KotlinStringValueObject::class))
        }

        @Test
        fun `should correctly register Instant value object type`() {
            context.type(KotlinInstantValueObject::class)

            verifyForcedType(CompositeDefinition(
                InstantConverterDefinition,
                ValueObjectDefinition(KotlinInstantValueObject::class)
            ))
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

            verifyForcedType(EnumDefinition("String", KotlinEnum::class))
        }
    }

    @Nested
    inner class ValueObject {

        @Test
        fun `should correctly register kotlin value object type with custom converter`() {
            context.valueObject(KotlinConverter::class, KotlinStringValueObject::class)

            verifyForcedType(CompositeDefinition(
                SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class),
                ValueObjectDefinition(KotlinStringValueObject::class)
            ))
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
            context.converter(KotlinConverter::class)

            verifyForcedType(SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class))
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

            verifyForcedType(SimpleConverterDefinition(Int::class, String::class, JavaConverter::class))
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

            verifyForcedType(CustomConverterDefinition(KotlinJooqConverter::class, String::class))
        }
    }

    @Nested
    inner class Custom {

        @Test
        fun `should correctly register custom converter`() {
            context.custom(Int::class, "custom")

            verifyForcedType(CustomConverterDefinition("custom", Int::class))
        }
    }

    private fun verifyForcedType(definition: ConverterDefinition) {
        verify(jooqContext).registerForcedType(definition.toForcedType(expression))
    }
}
