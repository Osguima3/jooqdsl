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
import io.osguima3.jooqdsl.plugin.converter.CompositeForcedType
import io.osguima3.jooqdsl.plugin.converter.ConverterForcedType
import io.osguima3.jooqdsl.plugin.converter.CustomForcedType
import io.osguima3.jooqdsl.plugin.converter.EnumForcedType
import io.osguima3.jooqdsl.plugin.converter.IForcedType
import io.osguima3.jooqdsl.plugin.converter.InstantForcedType
import io.osguima3.jooqdsl.plugin.converter.JooqConverterForcedType
import io.osguima3.jooqdsl.plugin.converter.ValueObjectForcedType
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
        null -> register(EnumForcedType(context, userType))
        else -> register(EnumForcedType(databaseType, userType))
    }

    override fun <T : Any, U : Any> valueObject(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<*>,
        databaseType: KClass<T>,
        valueType: KClass<U>
    ) {
        register(CompositeForcedType(
            ConverterForcedType(databaseType, valueType, converter),
            ValueObjectForcedType(valueType, userType)
        ))
    }

    override fun <T : Any, U : Any> converter(
        converter: KClass<out Converter<T, U>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ) {
        register(ConverterForcedType(databaseType, userType, converter))
    }

    override fun converter(converter: KClass<out JooqConverter<*, *>>) {
        register(JooqConverterForcedType(converter))
    }

    override fun custom(userType: KClass<*>, converter: String) {
        register(CustomForcedType(converter, userType))
    }

    private fun resolve(userType: KClass<*>) = when {
        userType.isPrimitive -> Unit // No need to register
        userType.isEnum -> register(EnumForcedType(context, userType))
        userType.isValueObject -> resolveValueObject(userType)
        userType == Instant::class -> register(InstantForcedType)
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun resolveValueObject(userType: KClass<*>, value: KClass<*> = userType.valueType) = when {
        value.isPrimitive -> register(ValueObjectForcedType(userType))
        value == Instant::class -> register(CompositeForcedType(InstantForcedType, ValueObjectForcedType(userType)))
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun register(forcedTypeData: IForcedType) {
        context.registerForcedType(".*\\.$tableName\\.$name", forcedTypeData)
    }
}
