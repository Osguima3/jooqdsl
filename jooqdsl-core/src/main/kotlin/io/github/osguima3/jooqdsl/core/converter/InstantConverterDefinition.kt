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
import io.github.osguima3.jooqdsl.core.kotlinClassName
import org.jooq.codegen.Language
import java.time.Instant
import java.time.OffsetDateTime

object InstantConverterDefinition : NullableConverterDefinition {

    override val fromType = OffsetDateTime::class

    override val toType = Instant::class

    override val Language.from: String
        get() = when (this) {
            Language.JAVA -> "${OffsetDateTime::class.javaClassName}::toInstant"
            Language.KOTLIN -> "${OffsetDateTime::class.kotlinClassName}::toInstant"
            else -> throw IllegalArgumentException("Unsupported language: $this")
        }

    override val Language.to: String
        get() = when (this) {
            Language.JAVA -> "i -> ${OffsetDateTime::class.javaClassName}.ofInstant(i, java.time.ZoneOffset.UTC)"
            Language.KOTLIN -> "{ ${OffsetDateTime::class.kotlinClassName}.ofInstant(it, java.time.ZoneOffset.UTC) }"
            else -> throw IllegalArgumentException("Unsupported language: $this")
        }
}
