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

package io.github.osguima3.jooqdsl.core.types

import io.github.osguima3.jooqdsl.model.converter.Converter
import io.github.osguima3.jooqdsl.model.converter.SimpleConverter
import java.time.Instant
import org.jooq.Converter as JooqConverter

enum class KotlinEnum
data class KotlinStringValueObject(val value: String)
data class KotlinInstantValueObject(val value: Instant)
data class KotlinUnsupportedValueObject(val value: KotlinEnum)
data class KotlinUnsupportedObject(val field1: String, val field2: String)

object KotlinConverter : SimpleConverter<Int, String>(Int::toString, String::toInt)

object KotlinJooqConverter : JooqConverter<Int, String> {

    override fun from(databaseObject: Int): String = ""
    override fun to(userObject: String): Int = 0
    override fun fromType() = Int::class.java
    override fun toType() = String::class.java
    private fun readResolve(): Any = KotlinJooqConverter
}

class KotlinInvalidConverter : Converter<Int, String> {
    override fun from(databaseObject: Int): String = ""
    override fun to(userObject: String): Int = 0
}
