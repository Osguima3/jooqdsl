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

import io.osguima3.jooqdsl.plugin.context.JooqContext
import java.util.function.Function
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter

class ConverterBuilder(private val context: JooqContext) {

    internal fun simple(userType: KClass<*>, databaseType: KClass<*>, from: String, to: String) =
        "org.jooq.Converter.ofNullable(${databaseType.canonical}.class, ${userType.simple}.class, $from, $to)"

    internal fun enum(
        userType: KClass<*>,
        databaseType: String? = "${context.targetPackage}.enums.${userType.simple}"
    ) = "new org.jooq.impl.EnumConverter<>($databaseType.class, ${userType.simple}.class)"

    internal fun valueObject(userType: KClass<*>) =
        simple(userType, userType.valueType, userType.ctor, userType.valueField)

    internal fun valueObject(userType: KClass<*>, databaseType: KClass<*>, converter: KClass<*>) =
        valueObject(userType, databaseType, "${converter.instance}::from", "${converter.instance}::to")

    internal fun valueObject(userType: KClass<*>, databaseType: KClass<*>, from: String, to: String) = simple(
        userType = userType, databaseType = databaseType,
        from = "(${funCast(databaseType.canonical, userType.valueType.canonical)} $from).andThen(${userType.ctor})",
        to = "(${funCast(userType.simple, userType.valueType.canonical)} ${userType.valueField}).andThen($to)")

    internal fun adapter(userType: KClass<*>, databaseType: KClass<*>, converter: KClass<*>) =
        simple(userType, databaseType, "${converter.instance}::from", "${converter.instance}::to")

    private fun funCast(from: String, to: String) =
        "(${Function::class.canonical}<$from, $to>)"

    private val KClass<*>.simple get() = javaObjectType.simpleName

    private val KClass<*>.canonical get() = javaObjectType.canonicalName

    private val KClass<*>.instance get() = "$canonical.INSTANCE"

    private val KClass<*>.ctor get() = "$simple::new"

    private val KClass<*>.valueField get() = "$simple::${valueGetter.name}"

    private val KClass<*>.valueType get() = valueGetter.returnType.kotlin

    private val KClass<*>.valueGetter
        get() = declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()
}
