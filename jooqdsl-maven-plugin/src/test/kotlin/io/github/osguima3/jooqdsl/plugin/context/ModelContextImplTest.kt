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

import io.github.osguima3.jooqdsl.plugin.converter.CompositeDefinition
import io.github.osguima3.jooqdsl.plugin.converter.EnumDefinition
import io.github.osguima3.jooqdsl.plugin.converter.InstantConverterDefinition
import io.github.osguima3.jooqdsl.plugin.converter.SimpleConverterDefinition
import io.github.osguima3.jooqdsl.plugin.converter.ValueObjectDefinition
import io.github.osguima3.jooqdsl.plugin.qualified
import io.github.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.github.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.github.osguima3.jooqdsl.plugin.types.KotlinInstantValueObject
import io.github.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
import org.assertj.core.api.Assertions.assertThat
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Target
import org.junit.jupiter.api.Test
import java.time.Instant

class ModelContextImplTest {

    private val expression = ".*\\.table\\.field"
    private val sourcePackage = "io.github.osguima3.jooqdsl.plugin.types"
    private val targetPackage = "io.github.osguima3.target.package"

    private val forcedTypes = mutableListOf<ForcedType>()
    private val generator = Generator().apply {
        database = Database().also { it.forcedTypes = forcedTypes }
        target = Target().apply { packageName = targetPackage }
    }

    private val context = ModelContextImpl(generator)

    @Test
    fun `should correctly register enum`() {
        val forcedType = EnumDefinition(KotlinEnum::class, context.targetPackage)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.includeExpression = expression
            it.userType = "$sourcePackage.KotlinEnum"
            it.converter = "new org.jooq.impl.EnumConverter<>(" +
                "$targetPackage.enums.KotlinEnum.class, $sourcePackage.KotlinEnum.class)"
        })
    }

    @Test
    fun `should correctly register enum with custom database type`() {
        val forcedType = EnumDefinition("String", KotlinEnum::class)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.includeExpression = expression
            it.userType = "$sourcePackage.KotlinEnum"
            it.converter = "new org.jooq.impl.EnumConverter<>(String.class, $sourcePackage.KotlinEnum.class)"
        })
    }

    @Test
    fun `should correctly register instant`() {
        context.registerForcedType(
            forcedType = InstantConverterDefinition.toForcedType(expression)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.includeExpression = expression
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
            it.includeExpression = expression
            it.userType = "$sourcePackage.KotlinStringValueObject"
            it.converter = "org.jooq.Converter.ofNullable(" +
                "java.lang.String.class, $sourcePackage.KotlinStringValueObject.class, " +
                "$sourcePackage.KotlinStringValueObject::new, " +
                "$sourcePackage.KotlinStringValueObject::getValue)"
        })
    }

    @Test
    fun `should correctly register converter`() {
        val forcedType = SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class)
        context.registerForcedType(
            forcedType = forcedType.toForcedType(expression)
        )

        assertThat(forcedTypes).containsExactly(ForcedType().also {
            it.includeExpression = expression
            it.userType = String::class.qualified
            it.converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, java.lang.String.class, " +
                "$sourcePackage.KotlinConverter.INSTANCE::from, " +
                "$sourcePackage.KotlinConverter.INSTANCE::to)"
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
            it.includeExpression = expression
            it.userType = "$sourcePackage.KotlinInstantValueObject"
            it.converter = "org.jooq.Converters.of(" +
                "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, ${Instant::class.qualified}.class, " +
                "java.time.OffsetDateTime::toInstant, " +
                "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)), " +
                "org.jooq.Converter.ofNullable(java.time.Instant.class, $sourcePackage.KotlinInstantValueObject.class, " +
                "$sourcePackage.KotlinInstantValueObject::new, " +
                "$sourcePackage.KotlinInstantValueObject::getValue))"
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
            it.includeExpression = expression
            it.userType = "$sourcePackage.KotlinStringValueObject"
            it.converter = "org.jooq.Converters.of(" +
                "org.jooq.Converter.ofNullable(java.lang.Integer.class, java.lang.String.class, " +
                "$sourcePackage.KotlinConverter.INSTANCE::from, " +
                "$sourcePackage.KotlinConverter.INSTANCE::to), " +
                "org.jooq.Converter.ofNullable(java.lang.String.class, $sourcePackage.KotlinStringValueObject.class, " +
                "$sourcePackage.KotlinStringValueObject::new, " +
                "$sourcePackage.KotlinStringValueObject::getValue))"
        })
    }
}
