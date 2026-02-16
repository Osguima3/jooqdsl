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
import io.github.osguima3.jooqdsl.core.kotlinClassRef
import org.jooq.codegen.Language
import kotlin.reflect.KClass

data class EnumDefinition(private val toType: KClass<*>, private val fromClass: String? = null) : ForcedTypeDefinition {

    override val Language.userType get() = toType.javaClassName

    override fun Language.converter(targetPackage: String, root: Boolean): String = when (this) {
        Language.JAVA -> "new org.jooq.impl.EnumConverter<>(${fromClass(targetPackage)}, ${toClass(root)})"
        Language.KOTLIN -> "org.jooq.impl.EnumConverter(${fromClass(targetPackage)}, ${toClass(root)})"
        else -> error("Unsupported language: $this")
    }

    fun Language.fromClass(targetPackage: String) = when (this) {
        Language.JAVA -> fromClass?.let { "$it.class" } ?: "$targetPackage.enums.${toType.simpleName}.class"
        Language.KOTLIN -> fromClass?.let { "$it::class.java" } ?: "$targetPackage.enums.${toType.simpleName}::class.java"
        else -> error("Unsupported language: $this")
    }

    private fun Language.toClass(root: Boolean): String = when (this) {
        Language.JAVA -> if (root) "${toType.javaObjectType.simpleName}.class" else toType.javaClassRef
        Language.KOTLIN -> if (root) "${toType.simpleName}::class.java" else toType.kotlinClassRef
        else -> error("Unsupported language: $this")
    }
}
