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

package io.github.osguima3.jooqdsl.core.converter

import io.github.osguima3.jooqdsl.core.javaClassName
import io.github.osguima3.jooqdsl.core.javaClassRef
import io.github.osguima3.jooqdsl.core.kotlinClassName
import io.github.osguima3.jooqdsl.core.kotlinClassRef
import org.jooq.codegen.Language
import kotlin.reflect.KClass

interface NullableConverterDefinition : ForcedTypeDefinition {

    val toType: KClass<*>

    val fromType: KClass<*>

    val Language.from: String

    val Language.to: String

    override val Language.userType
        get() = when (this) {
            Language.JAVA -> toType.javaClassName
            Language.KOTLIN -> toType.kotlinClassName
            else -> throw IllegalArgumentException("Unsupported language: $this")
        }

    override fun Language.converter(targetPackage: String, root: Boolean): String =
        "org.jooq.Converter.ofNullable(${asClass(fromType, false)}, ${asClass(toType, root)}, $from, $to)"

    fun Language.asClass(type: KClass<*>, root: Boolean): String = when (this) {
        Language.JAVA -> if (root) "${type.javaObjectType.simpleName}.class" else type.javaClassRef
        Language.KOTLIN -> if (root || isBasicType(type)) "${type.simpleName}::class.java" else type.kotlinClassRef
        else -> throw IllegalArgumentException("Unsupported language: $this")
    }

    fun isBasicType(type: KClass<*>): Boolean =
        type == String::class || type == Int::class || type == Long::class || type == Short::class ||
            type == Byte::class || type == Float::class || type == Double::class ||
            type == Boolean::class || type == Char::class
}
