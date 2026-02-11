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

import io.github.osguima3.jooqdsl.model.context.FieldContext
import io.github.osguima3.jooqdsl.model.converter.Converter
import io.github.osguima3.jooqdsl.core.converter.CompositeDefinition
import io.github.osguima3.jooqdsl.core.converter.CustomConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.core.converter.EnumDefinition
import io.github.osguima3.jooqdsl.core.converter.InstantConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.SimpleConverterDefinition
import io.github.osguima3.jooqdsl.core.converter.ValueObjectDefinition
import io.github.osguima3.jooqdsl.core.isEnum
import io.github.osguima3.jooqdsl.core.isPrimitive
import io.github.osguima3.jooqdsl.core.isValueObject
import io.github.osguima3.jooqdsl.core.valueType
import java.time.Instant
import kotlin.reflect.KClass
import org.jooq.Converter as JooqConverter

class FieldContextImpl(
    private val targetPackage: String
) : FieldContext {

    override fun type(userType: KClass<*>) =
        resolve(userType)

    override fun enum(userType: KClass<out Enum<*>>, databaseType: String?) = when (databaseType) {
        null -> EnumDefinition(userType, targetPackage)
        else -> EnumDefinition(databaseType, userType)
    }

    override fun <T : Any, U : Any> valueObject(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<*>,
        databaseType: KClass<T>,
        valueType: KClass<U>
    ) = CompositeDefinition(
        SimpleConverterDefinition(databaseType, valueType, converter),
        ValueObjectDefinition(valueType, userType)
    )

    override fun <T : Any, U : Any> converter(
        converter: KClass<out Converter<T, U>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ) = SimpleConverterDefinition(databaseType, userType, converter)

    override fun <U : Any> converter(converter: KClass<out JooqConverter<*, U>>, userType: KClass<U>) =
        CustomConverterDefinition(converter, userType)

    override fun custom(userType: KClass<*>, converter: String) =
        CustomConverterDefinition(converter, userType)

    private fun resolve(userType: KClass<*>) = when {
        userType.isPrimitive -> SkippedDefinition // No need to register
        userType.isEnum -> EnumDefinition(userType, targetPackage)
        userType.isValueObject -> resolveValueObject(userType)
        userType == Instant::class -> InstantConverterDefinition
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun resolveValueObject(userType: KClass<*>, value: KClass<*> = userType.valueType) = when {
        value.isPrimitive -> ValueObjectDefinition(userType)
        value == Instant::class -> CompositeDefinition(InstantConverterDefinition, ValueObjectDefinition(userType))
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }
}
