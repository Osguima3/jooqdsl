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

import io.osguima3.jooqdsl.plugin.converter.CompositeForcedType
import io.osguima3.jooqdsl.plugin.converter.ConverterForcedType
import io.osguima3.jooqdsl.plugin.converter.EnumForcedType
import io.osguima3.jooqdsl.plugin.converter.InstantForcedType
import io.osguima3.jooqdsl.plugin.converter.ValueObjectForcedType
import io.osguima3.jooqdsl.plugin.qualified
import io.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.osguima3.jooqdsl.plugin.types.KotlinInstantValueObject
import io.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
import org.assertj.core.api.Assertions.assertThat
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.Instant

class ModelContextImplTest {

    private val expression = ".*\\.table\\.field"
    private val targetPackage = "io.osguima3.project.package"

    private val forcedTypes = mutableListOf<ForcedType>()
    private val configuration = mock<Configuration> {
        on { it.generator.database.forcedTypes } doReturn forcedTypes
        on { it.generator.target.packageName } doReturn targetPackage
    }

    private val context = ModelContextImpl(configuration)

    @Test
    fun `should correctly register enum`() {
        context.registerForcedType(
            expression = expression,
            forcedType = EnumForcedType(context, KotlinEnum::class)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = KotlinEnum::class.qualified
            it.converter = "new org.jooq.impl.EnumConverter<>(" +
                "$targetPackage.enums.KotlinEnum.class, ${KotlinEnum::class.qualified}.class)"
        })
    }

    @Test
    fun `should correctly register enum with custom database type`() {
        context.registerForcedType(
            expression = expression,
            forcedType = EnumForcedType("String", KotlinEnum::class)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = KotlinEnum::class.qualified
            it.converter = "new org.jooq.impl.EnumConverter<>(String.class, ${KotlinEnum::class.qualified}.class)"
        })
    }

    @Test
    fun `should correctly register instant`() {
        context.registerForcedType(
            expression = expression,
            forcedType = InstantForcedType
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = Instant::class.qualified
            it.converter = "org.jooq.Converter.ofNullable(" +
                "java.time.OffsetDateTime.class, ${Instant::class.qualified}.class, " +
                "java.time.OffsetDateTime::toInstant, " +
                "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC))"
        })
    }

    @Test
    fun `should correctly register value object`() {
        context.registerForcedType(
            expression = expression,
            forcedType = ValueObjectForcedType(KotlinStringValueObject::class)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = KotlinStringValueObject::class.qualified
            it.converter = "org.jooq.Converter.ofNullable(" +
                "java.lang.String.class, ${KotlinStringValueObject::class.qualified}.class, " +
                "${KotlinStringValueObject::class.qualified}::new, " +
                "${KotlinStringValueObject::class.qualified}::getValue)"
        })
    }

    @Test
    fun `should correctly register converter`() {
        context.registerForcedType(
            expression = expression,
            forcedType = ConverterForcedType(Int::class, String::class, KotlinConverter::class)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = String::class.qualified
            it.converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, java.lang.String.class, " +
                "${KotlinConverter::class.qualified}.INSTANCE::from, " +
                "${KotlinConverter::class.qualified}.INSTANCE::to)"
        })
    }

    @Test
    fun `should correctly register composite value object + instant`() {
        context.registerForcedType(
            expression = expression,
            forcedType = CompositeForcedType(
                InstantForcedType,
                ValueObjectForcedType(KotlinInstantValueObject::class)
            )
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = KotlinInstantValueObject::class.qualified
            it.converter = "org.jooq.Converters.of(" +
                "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, ${Instant::class.qualified}.class, " +
                "java.time.OffsetDateTime::toInstant, " +
                "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)), " +
                "org.jooq.Converter.ofNullable(java.time.Instant.class, ${KotlinInstantValueObject::class.qualified}.class, " +
                "${KotlinInstantValueObject::class.qualified}::new, " +
                "${KotlinInstantValueObject::class.qualified}::getValue))"
        })
    }

    @Test
    fun `should correctly register composite value object + converter`() {
        context.registerForcedType(
            expression = expression,
            forcedType = CompositeForcedType(
                ConverterForcedType(Int::class, String::class, KotlinConverter::class),
                ValueObjectForcedType(KotlinStringValueObject::class)
            )
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.expression = expression
            it.userType = KotlinStringValueObject::class.qualified
            it.converter = "org.jooq.Converters.of(" +
                "org.jooq.Converter.ofNullable(java.lang.Integer.class, java.lang.String.class, " +
                "${KotlinConverter::class.qualified}.INSTANCE::from, " +
                "${KotlinConverter::class.qualified}.INSTANCE::to), " +
                "org.jooq.Converter.ofNullable(java.lang.String.class, ${KotlinStringValueObject::class.qualified}.class, " +
                "${KotlinStringValueObject::class.qualified}::new, " +
                "${KotlinStringValueObject::class.qualified}::getValue))"
        })
    }
}