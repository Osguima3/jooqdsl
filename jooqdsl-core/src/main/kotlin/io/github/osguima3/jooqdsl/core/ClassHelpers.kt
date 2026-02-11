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

package io.github.osguima3.jooqdsl.core

import io.github.osguima3.jooqdsl.core.converter.INSTANCE_FIELD
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

internal val KClass<*>.isPrimitive
    get() = when (this) {
        BigDecimal::class, String::class, UUID::class -> true
        else -> javaPrimitiveType != null
    }

internal val KClass<*>.isEnum
    get() = isSubclassOf(Enum::class)

internal val KClass<*>.isValueObject
    get() = isData && declaredMemberProperties.size == 1

internal val KClass<*>.isObjectType
    get() = objectInstance != null || java.declaredFields.any {
        it.name == INSTANCE_FIELD && it.type == java && Modifier.isStatic(it.modifiers)
    }

internal val KClass<*>.simple: String
    get() = javaObjectType.simpleName

internal val KClass<*>.qualified: String
    get() = javaObjectType.canonicalName

internal val KClass<*>.valueField: String
    get() = valueGetter.name

internal val KClass<*>.valueType: KClass<out Any>
    get() = valueGetter.returnType.kotlin

internal val KClass<*>.valueGetter: Method
    get() = if (!isValueObject) {
        throw IllegalArgumentException("Type $this is not a value object.")
    } else {
        declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()
    }
