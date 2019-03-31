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

package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.model.converter.Converter
import io.osguima3.jooqdsl.plugin.context.ModelContextImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter

class ConverterBuilder(private val context: ModelContextImpl) {

    internal fun simple(converter: KClass<out Converter<*, *>>) =
        "new ${converter.qualifiedName}()"

    internal fun instant() =
        "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, java.time.Instant.class, " +
            "java.time.OffsetDateTime::toInstant, i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC))"

    internal fun enum(userType: KClass<*>, databaseType: String? = null) =
        "new org.jooq.impl.EnumConverter<>(" +
            "${databaseType ?: "${context.targetPackage}.enums.${userType.simpleName}"}.class, ${userType.simple})"

    internal fun valueObject(userType: KClass<*>) =
        "org.jooq.Converter.ofNullable(${userType.valueType.qualified}, ${userType.simple}, " +
            "${userType.ctor}, ${userType.field})"

    internal fun valueObject(userType: KClass<*>, databaseType: KClass<*>, converter: String) =
        "new ${context.converterPackage}.ValueObjectConverter<>($converter, " +
            "${userType.ctor}, ${userType.field}, ${databaseType.qualified}, ${userType.simple})"

    internal fun adapter(userType: KClass<*>, databaseType: KClass<*>, converter: String) =
        "new ${context.converterPackage}.ConverterAdapter<>($converter, " +
            "${databaseType.qualified}, ${userType.simple})"

    private val KClass<*>.simple get() = "$simpleName.class"

    private val KClass<*>.qualified get() = "${javaObjectType.canonicalName}.class"

    private val KClass<*>.ctor get() = "$simpleName::new"

    private val KClass<*>.field get() = "$simpleName::${valueField.name}"

    private val KClass<*>.valueField
        get() = declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()

    private val KClass<*>.valueType
        get() = valueField.returnType.kotlin
}
