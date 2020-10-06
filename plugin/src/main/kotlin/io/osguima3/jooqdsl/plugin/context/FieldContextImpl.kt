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
import io.osguima3.jooqdsl.plugin.converter.ConverterBuilder
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

class FieldContextImpl(
    private val context: JooqContext,
    private val tableName: String,
    private val name: String
) : FieldContext {

    private val instanceField = "INSTANCE"

    private val builder = ConverterBuilder(context)

    override fun type(userType: KClass<*>) =
        resolve(userType)

    override fun enum(userType: KClass<out Enum<*>>, databaseType: String?) =
        register(userType, builder.enum(userType, databaseType))

    override fun <T : Any, U : Any> valueObject(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<*>,
        databaseType: KClass<T>,
        valueType: KClass<U>
    ) = if (!userType.isValueObject) {
        throw IllegalArgumentException("Type $userType is not a value object.")
    } else if (valueType != userType.valueType) {
        throw IllegalArgumentException(
            "$userType.${userType.valueField.name}(): ${userType.valueType} is not of type $valueType.")
    } else {
        registerValueObject(userType, databaseType, converter)
    }

    override fun <T : Any, U : Any> custom(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<U>,
        databaseType: KClass<T>
    ) = if (!converter.isObjectType) {
        throw IllegalArgumentException(
            "Converter $converter should be a kotlin `object` or have an $instanceField singleton field.")
    } else {
        registerAdapter(userType, databaseType, converter)
    }

    private fun resolve(userType: KClass<*>) = when {
        userType.isPrimitive -> Unit // No need to register
        userType.isEnum -> registerEnum(userType)
        userType.isValueObject -> resolveValueObject(userType)
        userType == Instant::class -> register(userType, OffsetDateTime::class, instantFrom, instantTo)
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun resolveValueObject(userType: KClass<*>, valueType: KClass<*> = userType.valueType) = when {
        valueType.isPrimitive -> registerValueObject(userType)
        valueType == Instant::class -> registerValueObject(userType, OffsetDateTime::class, instantFrom, instantTo)
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun register(userType: KClass<*>, converter: String) {
        context.registerForcedType(".*\\.$tableName\\.$name", userType, converter)
    }

    private fun register(userType: KClass<*>, databaseType: KClass<*>, from: String, to: String) {
        register(userType, builder.simple(userType, databaseType, from, to))
    }

    private fun registerEnum(userType: KClass<*>) {
        register(userType, builder.enum(userType))
    }

    private fun registerValueObject(userType: KClass<*>) {
        register(userType, builder.valueObject(userType))
    }

    private fun registerValueObject(userType: KClass<*>, databaseType: KClass<*>, converter: KClass<*>) {
        register(userType, builder.valueObject(userType, databaseType, converter))
    }

    private fun registerValueObject(userType: KClass<*>, databaseType: KClass<*>, from: String, to: String) {
        register(userType, builder.valueObject(userType, databaseType, from, to))
    }

    private fun registerAdapter(userType: KClass<*>, databaseType: KClass<*>, converter: KClass<*>) {
        register(userType, builder.adapter(userType, databaseType, converter))
    }

    private val instantFrom = "java.time.OffsetDateTime::toInstant"

    private val instantTo = "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)"

    private val KClass<*>.isPrimitive
        get() = when (this) {
            BigDecimal::class, String::class, UUID::class -> true
            else -> javaPrimitiveType != null
        }

    private val KClass<*>.isEnum
        get() = isSubclassOf(Enum::class)

    private val KClass<*>.isValueObject
        get() = isData && declaredMemberProperties.size == 1

    private val KClass<*>.isObjectType
        get() = objectInstance != null || java.declaredFields.any {
            it.name == instanceField && it.type == java && Modifier.isStatic(it.modifiers)
        }

    private val KClass<*>.valueField
        get() = declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()

    private val KClass<*>.valueType
        get() = valueField.returnType.kotlin
}
