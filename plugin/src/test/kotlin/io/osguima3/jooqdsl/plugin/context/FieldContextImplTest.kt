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
import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.model.context.valueObject
import io.osguima3.jooqdsl.plugin.TestEnum
import io.osguima3.jooqdsl.plugin.TestInstantValueObject
import io.osguima3.jooqdsl.plugin.TestInvalidConverter
import io.osguima3.jooqdsl.plugin.TestJavaConverter
import io.osguima3.jooqdsl.plugin.TestKotlinConverter
import io.osguima3.jooqdsl.plugin.TestUnsupportedObject
import io.osguima3.jooqdsl.plugin.TestUnsupportedValueObject
import io.osguima3.jooqdsl.plugin.TestValueObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    inner class Type {

        @Test
        fun `should skip primitive types`() {
            context.type(Int::class)

            verifyZeroInteractions(jooqContext)
        }

        @Test
        fun `should skip boxed primitive types`() {
            context.type(Integer::class)

            verifyZeroInteractions(jooqContext)
        }

        @Test
        fun `should skip BigDecimal type`() {
            context.type(BigDecimal::class)

            verifyZeroInteractions(jooqContext)
        }

        @Test
        fun `should skip String type`() {
            context.type(String::class)

            verifyZeroInteractions(jooqContext)
        }

        @Test
        fun `should skip UUID type`() {
            context.type(UUID::class)

            verifyZeroInteractions(jooqContext)
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

        @Test
        fun `should correctly register enum types`() {
            context.type(TestEnum::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = TestEnum::class,
                converter = "new org.jooq.impl.EnumConverter<>(" +
                    "$targetPackage.enums.TestEnum.class, TestEnum.class)"
            )
        }

        @Test
        fun `should correctly register simple value object types`() {
            context.type(TestValueObject::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = TestValueObject::class,
                converter = "org.jooq.Converter.ofNullable(" +
                    "java.lang.String.class, TestValueObject.class, " +
                    "TestValueObject::new, TestValueObject::getValue)"
            )
        }

        @Test
        fun `should correctly register Instant value object type`() {
            context.type(TestInstantValueObject::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = TestInstantValueObject::class,
                converter = "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, TestInstantValueObject.class, " +
                    "(${functionCast(OffsetDateTime::class.qualified, Instant::class.qualified)}" +
                    "java.time.OffsetDateTime::toInstant).andThen(TestInstantValueObject::new), " +
                    "(${functionCast(TestInstantValueObject::class.simple, Instant::class.qualified)}" +
                    "TestInstantValueObject::getValue)" +
                    ".andThen(i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)))"
            )
        }

        @Test
        fun `should throw IllegalArgumentException if no mapper is available`() {
            assertThrows<IllegalArgumentException> {
                context.type(TestUnsupportedObject::class)
            }
        }

        @Test
        fun `should throw IllegalArgumentException if no value object mapper is available`() {
            assertThrows<IllegalArgumentException> {
                context.type(TestUnsupportedValueObject::class)
            }
        }
    }

    @Nested
    inner class Enum {

        @Test
        fun `should correctly register enum type with custom database type`() {
            context.enum(TestEnum::class, "String")

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = TestEnum::class,
                converter = "new org.jooq.impl.EnumConverter<>(String.class, TestEnum.class)"
            )
        }
    }

    @Nested
    inner class ValueObject {

        @Test
        fun `should correctly register value object type with custom converter`() {
            context.valueObject(TestKotlinConverter::class, TestValueObject::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = TestValueObject::class,
                converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, TestValueObject.class, " +
                    "(${functionCast(Integer::class.qualified, String::class.qualified)}" +
                    "${TestKotlinConverter::class.qualified}.INSTANCE::from).andThen(TestValueObject::new), " +
                    "(${functionCast(TestValueObject::class.simple, String::class.qualified)}" +
                    "TestValueObject::getValue).andThen(${TestKotlinConverter::class.qualified}.INSTANCE::to))"
            )
        }

        @Test
        fun `should throw IllegalArgumentException if type is not a value type`() {
            assertThrows<IllegalArgumentException> {
                context.valueObject(TestKotlinConverter::class, String::class)
            }
        }

        @Test
        fun `should throw IllegalArgumentException if value field type does not match converter from type`() {
            assertThrows<IllegalArgumentException> {
                context.valueObject(TestKotlinConverter::class, TestInstantValueObject::class)
            }
        }
    }

    @Nested
    inner class Custom {

        @Test
        fun `should correctly register kotlin-style converter`() {
            context.custom(TestKotlinConverter::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = String::class,
                converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                    "${TestKotlinConverter::class.qualified}.INSTANCE::from, " +
                    "${TestKotlinConverter::class.qualified}.INSTANCE::to)"
            )
        }

        @Test
        fun `should correctly register java-style converter`() {
            context.custom(TestJavaConverter::class)

            verify(jooqContext).registerForcedType(
                expression = expression,
                userType = String::class,
                converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                    "${TestJavaConverter::class.qualified}.INSTANCE::from, " +
                    "${TestJavaConverter::class.qualified}.INSTANCE::to)"
            )
        }

        @Test
        fun `should throw IllegalArgumentException if converter does not have an INSTANCE singleton field`() {
            assertThrows<IllegalArgumentException> {
                context.custom(TestInvalidConverter::class)
            }
        }
    }

    fun functionCast(from: String, to: String): String = "(${Function::class.qualified}<$from, $to>) "

    private val KClass<*>.simple get() = javaObjectType.simpleName

    private val KClass<*>.qualified get() = javaObjectType.canonicalName
}
