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

import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.model.context.valueObject
import io.osguima3.jooqdsl.plugin.types.JavaConverter
import io.osguima3.jooqdsl.plugin.types.JavaInvalidConverter
import io.osguima3.jooqdsl.plugin.types.JavaUnsupportedObject
import io.osguima3.jooqdsl.plugin.types.JavaValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.osguima3.jooqdsl.plugin.types.KotlinInstantValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinInvalidConverter
import io.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinUnsupportedObject
import io.osguima3.jooqdsl.plugin.types.KotlinUnsupportedValueObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Function
import kotlin.reflect.KClass

class FieldContextImplTest {

    private val expression = ".*\\.table\\.field"
    private val targetPackage = "io.osguima3.project.package"

    private val jooqContext = mock<JooqContext>().also {
        whenever(it.targetPackage).thenReturn(targetPackage)
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

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = Instant::class,
                converter = "org.jooq.Converter.ofNullable(" +
                    "java.time.OffsetDateTime.class, Instant.class, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC))"
            )
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

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = KotlinEnum::class,
                converter = "new org.jooq.impl.EnumConverter<>($targetPackage.enums.KotlinEnum.class, KotlinEnum.class)"
            )
        }

        @Test
        fun `should correctly register simple value object types`() {
            context.type(KotlinStringValueObject::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = KotlinStringValueObject::class,
                converter = "org.jooq.Converter.ofNullable(" +
                    "java.lang.String.class, KotlinStringValueObject.class, " +
                    "KotlinStringValueObject::new, KotlinStringValueObject::getValue)"
            )
        }

        @Test
        fun `should correctly register Instant value object type`() {
            context.type(KotlinInstantValueObject::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = KotlinInstantValueObject::class,
                converter = "org.jooq.Converter.ofNullable(" +
                    "java.time.OffsetDateTime.class, KotlinInstantValueObject.class, " +
                    "(${functionCast(OffsetDateTime::class.qualified, Instant::class.qualified)}" +
                    "java.time.OffsetDateTime::toInstant).andThen(KotlinInstantValueObject::new), " +
                    "(${functionCast(KotlinInstantValueObject::class.simple, Instant::class.qualified)}" +
                    "KotlinInstantValueObject::getValue)" +
                    ".andThen(i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)))"
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

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = KotlinEnum::class,
                converter = "new org.jooq.impl.EnumConverter<>(String.class, KotlinEnum.class)"
            )
        }
    }

    @Nested
    inner class ValueObject {

        @Test
        fun `should correctly register kotlin value object type with custom converter`() {
            context.valueObject(KotlinConverter::class, KotlinStringValueObject::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = KotlinStringValueObject::class,
                converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, KotlinStringValueObject.class, " +
                    "(${functionCast(Integer::class.qualified, String::class.qualified)}" +
                    "${KotlinConverter::class.qualified}.INSTANCE::from).andThen(KotlinStringValueObject::new), " +
                    "(${functionCast(KotlinStringValueObject::class.simple, String::class.qualified)}" +
                    "KotlinStringValueObject::getValue).andThen(${KotlinConverter::class.qualified}.INSTANCE::to))"
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
    inner class Custom {

        @Test
        fun `should correctly register Kotlin converter`() {
            context.custom(KotlinConverter::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = String::class,
                converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                    "${KotlinConverter::class.qualified}.INSTANCE::from, " +
                    "${KotlinConverter::class.qualified}.INSTANCE::to)"
            )
        }

        @Test
        fun `should throw IllegalArgumentException if Kotlin converter is not an object`() {
            assertThrows<IllegalArgumentException> {
                context.custom(KotlinInvalidConverter::class)
            }
        }

        @Test
        fun `should correctly register Java converter`() {
            context.custom(JavaConverter::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = String::class,
                converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                    "${JavaConverter::class.qualified}.INSTANCE::from, " +
                    "${JavaConverter::class.qualified}.INSTANCE::to)"
            )
        }

        @Test
        fun `should throw IllegalArgumentException if Java converter does not have an INSTANCE singleton field`() {
            assertThrows<IllegalArgumentException> {
                context.custom(JavaInvalidConverter::class)
            }
        }
    }

    fun functionCast(from: String, to: String): String = "(${Function::class.qualified}<$from, $to>) "

    private val KClass<*>.simple get() = javaObjectType.simpleName

    private val KClass<*>.qualified get() = javaObjectType.canonicalName
}
