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
import io.github.osguima3.jooqdsl.core.converter.EnumDefinition
import io.github.osguima3.jooqdsl.core.converter.FieldDefinition
import io.github.osguima3.jooqdsl.core.converter.InstantConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.SimpleConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.core.converter.ValueObjectDefinition
import io.github.osguima3.jooqdsl.core.types.JavaConverter
import io.github.osguima3.jooqdsl.core.types.JavaRecord
import io.github.osguima3.jooqdsl.core.types.JavaRecordWithMethods
import io.github.osguima3.jooqdsl.core.types.JavaValueObject
import io.github.osguima3.jooqdsl.core.types.JavaValueObjectWithMethods
import io.github.osguima3.jooqdsl.core.types.KotlinBigDecimalValueObject
import io.github.osguima3.jooqdsl.core.types.KotlinConverter
import io.github.osguima3.jooqdsl.core.types.KotlinEnum
import io.github.osguima3.jooqdsl.core.types.KotlinInstantValueObject
import io.github.osguima3.jooqdsl.core.types.KotlinStringValueObject
import io.github.osguima3.jooqdsl.core.types.KotlinValueObjectWithMethods
import org.assertj.core.api.Assertions.assertThat
import org.jooq.codegen.JooqConfigurator
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Target
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JooqConfiguratorTest {

    private val tableName = "table"
    private val fieldName = "field"
    private val expression = ".*\\.table\\.field"
    private val sourcePackage = "io.github.osguima3.jooqdsl.core.types"
    private val targetPackage = "io.github.osguima3.target.package"

    private val forcedTypes = mutableListOf<ForcedType>()

    private fun buildConfiguration(generatorName: String): Configuration = Configuration().apply {
        generator = Generator().apply {
            name = generatorName
            database = Database().also { it.forcedTypes = forcedTypes }
            target = Target().apply { packageName = targetPackage }
        }
    }

    private val context = JooqConfigurator()

    @Nested
    inner class Java {

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should not configure skipped definitions`(generatorName: String) {
            val definition = SkippedDefinition

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).isEmpty()
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure enum`(generatorName: String) {
            val definition = EnumDefinition(KotlinEnum::class)

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinEnum"
                it.converter = "new org.jooq.impl.EnumConverter<>(" +
                    "$targetPackage.enums.KotlinEnum.class, KotlinEnum.class)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure enum with custom database type`(generatorName: String) {
            val definition = EnumDefinition(KotlinEnum::class, "String")

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinEnum"
                it.converter = "new org.jooq.impl.EnumConverter<>(String.class, KotlinEnum.class)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure instant`(generatorName: String) {
            val definition = InstantConverterDefinition

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "java.time.Instant"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.time.OffsetDateTime.class, Instant.class, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC))"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure class value object`(generatorName: String) {
            val definition = ValueObjectDefinition(JavaValueObject::class)

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaValueObject"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.lang.String.class, JavaValueObject.class, " +
                    "JavaValueObject::new, JavaValueObject::getValue)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure value object with additional methods`(generatorName: String) {
            val definition = ValueObjectDefinition(JavaValueObjectWithMethods::class)

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaValueObjectWithMethods"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.lang.String.class, JavaValueObjectWithMethods.class, " +
                    "JavaValueObjectWithMethods::new, JavaValueObjectWithMethods::getValue)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure record`(generatorName: String) {
            val definition = ValueObjectDefinition(JavaRecord::class)

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaRecord"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.lang.String.class, JavaRecord.class, " +
                    "JavaRecord::new, JavaRecord::value)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure record with additional methods`(generatorName: String) {
            val definition = ValueObjectDefinition(JavaRecordWithMethods::class)

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaRecordWithMethods"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.lang.String.class, JavaRecordWithMethods.class, " +
                    "JavaRecordWithMethods::new, JavaRecordWithMethods::value)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure converter`(generatorName: String) {
            val definition = SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class)

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "java.lang.String"
                it.converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                    "$sourcePackage.KotlinConverter.INSTANCE::from, $sourcePackage.KotlinConverter.INSTANCE::to)"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure composite value object + instant`(generatorName: String) {
            val definition = CompositeDefinition(
                InstantConverterDefinition,
                ValueObjectDefinition(KotlinInstantValueObject::class)
            )

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinInstantValueObject"
                it.converter = "org.jooq.Converters.of(" +
                    "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, java.time.Instant.class, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)), " +
                    "org.jooq.Converter.ofNullable(java.time.Instant.class, KotlinInstantValueObject.class, " +
                    "KotlinInstantValueObject::new, KotlinInstantValueObject::getValue))"
            })
        }

        @ParameterizedTest
        @ValueSource(strings = ["org.jooq.codegen.DefaultGenerator", "org.jooq.codegen.JavaGenerator"])
        fun `should correctly configure composite value object + converter`(generatorName: String) {
            val definition = CompositeDefinition(
                SimpleConverterDefinition(Int::class, String::class, JavaConverter::class),
                ValueObjectDefinition(JavaValueObject::class)
            )

            configure(definition, buildConfiguration(generatorName))

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaValueObject"
                it.converter = "org.jooq.Converters.of(" +
                    "org.jooq.Converter.ofNullable(java.lang.Integer.class, java.lang.String.class, " +
                    "$sourcePackage.JavaConverter.INSTANCE::from, " +
                    "$sourcePackage.JavaConverter.INSTANCE::to), " +
                    "org.jooq.Converter.ofNullable(java.lang.String.class, JavaValueObject.class, " +
                    "JavaValueObject::new, JavaValueObject::getValue))"
            })
        }
    }

    @Nested
    inner class Kotlin {

        private val configuration = buildConfiguration("org.jooq.codegen.KotlinGenerator")

        @Test
        fun `should not configure skipped definitions`() {
            val definition = SkippedDefinition

            configure(definition, configuration)

            assertThat(forcedTypes).isEmpty()
        }

        @Test
        fun `should correctly configure enum`() {
            val definition = EnumDefinition(KotlinEnum::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinEnum"
                it.converter = "org.jooq.impl.EnumConverter(" +
                    "$targetPackage.enums.KotlinEnum::class.java, KotlinEnum::class.java)"
            })
        }

        @Test
        fun `should correctly configure enum with custom database type`() {
            val definition = EnumDefinition(KotlinEnum::class, "String")

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinEnum"
                it.converter = "org.jooq.impl.EnumConverter(String::class.java, KotlinEnum::class.java)"
            })
        }

        @Test
        fun `should correctly configure instant`() {
            val definition = InstantConverterDefinition

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "java.time.Instant"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.time.OffsetDateTime::class.java, Instant::class.java, " +
                    "java.time.OffsetDateTime::toInstant, " +
                    "{ java.time.OffsetDateTime.ofInstant(it, java.time.ZoneOffset.UTC) })"
            })
        }

        @Test
        fun `should correctly configure java value object`() {
            val definition = ValueObjectDefinition(JavaValueObject::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaValueObject"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "String::class.java, JavaValueObject::class.java, " +
                    "::JavaValueObject, JavaValueObject::value)"
            })
        }

        @Test
        fun `should correctly configure java record`() {
            val definition = ValueObjectDefinition(JavaRecord::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.JavaRecord"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "String::class.java, JavaRecord::class.java, " +
                    "::JavaRecord, JavaRecord::value)"
            })
        }

        @Test
        fun `should correctly configure kotlin value object`() {
            val definition = ValueObjectDefinition(KotlinStringValueObject::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinStringValueObject"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "String::class.java, KotlinStringValueObject::class.java, " +
                    "::KotlinStringValueObject, KotlinStringValueObject::value)"
            })
        }

        @Test
        fun `should correctly configure kotlin value object for big decimal`() {
            val definition = ValueObjectDefinition(KotlinBigDecimalValueObject::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinBigDecimalValueObject"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "java.math.BigDecimal::class.java, KotlinBigDecimalValueObject::class.java, " +
                    "::KotlinBigDecimalValueObject, KotlinBigDecimalValueObject::value)"
            })
        }

        @Test
        fun `should correctly configure kotlin value object with additional methods`() {
            val definition = ValueObjectDefinition(KotlinValueObjectWithMethods::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinValueObjectWithMethods"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "String::class.java, KotlinValueObjectWithMethods::class.java, " +
                    "::KotlinValueObjectWithMethods, KotlinValueObjectWithMethods::value)"
            })
        }

        @Test
        fun `should correctly configure converter`() {
            val definition = SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class)

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "kotlin.String"
                it.converter = "org.jooq.Converter.ofNullable(" +
                    "Int::class.java, String::class.java, " +
                    "$sourcePackage.KotlinConverter::from, $sourcePackage.KotlinConverter::to)"
            })
        }

        @Test
        fun `should correctly configure composite value object + instant`() {
            val definition = CompositeDefinition(
                InstantConverterDefinition,
                ValueObjectDefinition(KotlinInstantValueObject::class)
            )

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinInstantValueObject"
                it.converter = "org.jooq.Converters.of(" +
                    "org.jooq.Converter.ofNullable(java.time.OffsetDateTime::class.java, " +
                    "java.time.Instant::class.java, java.time.OffsetDateTime::toInstant, " +
                    "{ java.time.OffsetDateTime.ofInstant(it, java.time.ZoneOffset.UTC) }), " +
                    "org.jooq.Converter.ofNullable(java.time.Instant::class.java, " +
                    "KotlinInstantValueObject::class.java, " +
                    "::KotlinInstantValueObject, KotlinInstantValueObject::value))"
            })
        }

        @Test
        fun `should correctly configure composite value object + converter`() {
            val definition = CompositeDefinition(
                SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class),
                ValueObjectDefinition(KotlinStringValueObject::class)
            )

            configure(definition, configuration)

            assertThat(forcedTypes).containsExactly(ForcedType().also {
                it.includeExpression = expression
                it.userType = "$sourcePackage.KotlinStringValueObject"
                it.converter = "org.jooq.Converters.of(" +
                    "org.jooq.Converter.ofNullable(Int::class.java, String::class.java, " +
                    "$sourcePackage.KotlinConverter::from, $sourcePackage.KotlinConverter::to), " +
                    "org.jooq.Converter.ofNullable(String::class.java, KotlinStringValueObject::class.java, " +
                    "::KotlinStringValueObject, KotlinStringValueObject::value))"
            })
        }
    }

    private fun configure(definition: FieldDefinition, configuration: Configuration) {
        context.configureField(tableName, fieldName, definition)
        context.apply(configuration)
    }
}
