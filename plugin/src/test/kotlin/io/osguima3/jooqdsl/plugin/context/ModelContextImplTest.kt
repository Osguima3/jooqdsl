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

import io.osguima3.jooqdsl.plugin.converter.CompositeDefinition
import io.osguima3.jooqdsl.plugin.converter.SimpleConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.EnumDefinition
import io.osguima3.jooqdsl.plugin.converter.InstantConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.ValueObjectDefinition
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
        val forcedType = EnumDefinition(context, KotlinEnum::class)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
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
        val forcedType = EnumDefinition("String", KotlinEnum::class)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
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
            forcedType = InstantConverterDefinition.toForcedType(expression)
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
        val forcedType = ValueObjectDefinition(KotlinStringValueObject::class)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
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
        val forcedType = SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
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
        val forcedType = CompositeDefinition(
            InstantConverterDefinition,
            ValueObjectDefinition(KotlinInstantValueObject::class)
        )
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
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
        val forcedType = CompositeDefinition(
            SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class),
            ValueObjectDefinition(KotlinStringValueObject::class)
        )
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
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
