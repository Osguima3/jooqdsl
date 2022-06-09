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

import io.osguima3.jooqdsl.model.context.FieldContext
import io.osguima3.jooqdsl.model.converter.Converter
import io.osguima3.jooqdsl.plugin.converter.CompositeDefinition
import io.osguima3.jooqdsl.plugin.converter.ConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.CustomConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.EnumDefinition
import io.osguima3.jooqdsl.plugin.converter.InstantConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.SimpleConverterDefinition
import io.osguima3.jooqdsl.plugin.converter.ValueObjectDefinition
import io.osguima3.jooqdsl.plugin.isEnum
import io.osguima3.jooqdsl.plugin.isPrimitive
import io.osguima3.jooqdsl.plugin.isValueObject
import io.osguima3.jooqdsl.plugin.valueType
import java.time.Instant
import kotlin.reflect.KClass
import org.jooq.Converter as JooqConverter

class FieldContextImpl(
    private val context: JooqContext,
    private val tableName: String,
    private val name: String
) : FieldContext {

    override fun type(userType: KClass<*>) =
        resolve(userType)

    override fun enum(userType: KClass<out Enum<*>>, databaseType: String?) = when (databaseType) {
        null -> register(EnumDefinition(context, userType))
        else -> register(EnumDefinition(databaseType, userType))
    }

    override fun <T : Any, U : Any> valueObject(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<*>,
        databaseType: KClass<T>,
        valueType: KClass<U>
    ) {
        register(CompositeDefinition(
            SimpleConverterDefinition(databaseType, valueType, converter),
            ValueObjectDefinition(valueType, userType)
        ))
    }

    override fun <T : Any, U : Any> converter(
        converter: KClass<out Converter<T, U>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ) {
        register(SimpleConverterDefinition(databaseType, userType, converter))
    }

    override fun <U : Any> converter(converter: KClass<out JooqConverter<*, U>>, userType: KClass<U>) {
        register(CustomConverterDefinition(converter, userType))
    }

    override fun custom(userType: KClass<*>, converter: String) {
        register(CustomConverterDefinition(converter, userType))
    }

    private fun resolve(userType: KClass<*>) = when {
        userType.isPrimitive -> Unit // No need to register
        userType.isEnum -> register(EnumDefinition(context, userType))
        userType.isValueObject -> resolveValueObject(userType)
        userType == Instant::class -> register(InstantConverterDefinition)
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun resolveValueObject(userType: KClass<*>, value: KClass<*> = userType.valueType) = when {
        value.isPrimitive -> register(ValueObjectDefinition(userType))
        value == Instant::class -> register(
            CompositeDefinition(InstantConverterDefinition, ValueObjectDefinition(userType)))
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun register(definition: ConverterDefinition) {
        context.registerForcedType(definition.toForcedType(".*\\.$tableName\\.$name"))
    }
}
