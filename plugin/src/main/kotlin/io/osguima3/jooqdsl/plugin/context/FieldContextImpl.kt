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
import io.osguima3.jooqdsl.plugin.converter.TemplateFile
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
    private val context: ModelContextImpl,
    private val tableName: String,
    private val name: String
) : FieldContext {

    private val builder = ConverterBuilder(context)

    private var initialized = false

    fun doConfigure(configure: FieldContext.() -> Unit) = if (initialized) {
        throw IllegalStateException("Field $name already initialized")
    } else {
        configure()
        initialized = true
    }

    override fun type(userType: KClass<*>) =
        resolve(userType)

    override fun enum(userType: KClass<out Enum<*>>, databaseType: String?) =
        register(userType, builder.enum(userType, databaseType))

    override fun <T : Any, U : Any> valueObject(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<*>,
        databaseType: KClass<T>,
        fieldType: KClass<U>
    ) = if (!userType.isValueObject) {
        throw IllegalArgumentException("Type $userType is not a value object.")
    } else if (fieldType != userType.fieldType) {
        throw IllegalArgumentException("$userType.${userType.singleField.name}(): ${userType.fieldType} " +
            "is not of type $fieldType.")
    } else {
        registerValueObject(databaseType, userType, builder.simple(converter), emptySet())
    }

    override fun <T : Any, U : Any> custom(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<U>,
        databaseType: KClass<T>
    ) = registerAdapter(databaseType, userType, builder.simple(converter))

    private fun resolve(userType: KClass<*>) = when {
        userType.isPrimitive -> Unit // No need to register
        userType.isEnum -> register(userType, builder.enum(userType))
        userType.isValueObject -> resolveValueObject(userType)
        userType == Instant::class -> register(userType, builder.instant())
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun resolveValueObject(userType: KClass<*>, fieldType: KClass<*> = userType.fieldType) = when {
        fieldType.isPrimitive -> register(userType, builder.valueObject(userType.javaObjectType.kotlin))
        fieldType == Instant::class -> registerValueObject(OffsetDateTime::class, userType, builder.instant())
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun registerValueObject(
        databaseType: KClass<*>,
        userType: KClass<*>,
        converter: String,
        templates: Set<TemplateFile> = setOf(TemplateFile.ADAPTER)
    ) = register(
        userType = userType,
        templates = templates + setOf(TemplateFile.VALUE_OBJECT),
        converter = builder.valueObject(userType, databaseType, converter)
    )

    private fun <T : Any, U : Any> registerAdapter(
        databaseType: KClass<T>,
        userType: KClass<U>,
        converter: String,
        templates: Set<TemplateFile> = emptySet()
    ) = register(
        userType = userType,
        templates = templates + setOf(TemplateFile.ADAPTER),
        converter = builder.adapter(userType, databaseType, converter)
    )

    private fun register(userType: KClass<*>, converter: String, templates: Set<TemplateFile> = emptySet()) {
        context.addTemplates(templates)
        context.registerForcedType(".*\\.$tableName\\.$name", userType, converter)
    }

    private val KClass<*>.isPrimitive
        get() = when (this) {
            BigDecimal::class, String::class, UUID::class -> true
            else -> javaPrimitiveType != null
        }

    private val KClass<*>.isEnum
        get() = isSubclassOf(Enum::class)

    private val KClass<*>.isValueObject
        get() = isData && declaredMemberProperties.size == 1

    private val KClass<*>.singleField
        get() = declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()

    private val KClass<*>.fieldType
        get() = singleField.returnType.kotlin
}
