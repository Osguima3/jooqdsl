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

import io.github.osguima3.jooqdsl.core.qualified
import io.github.osguima3.jooqdsl.core.valueField
import io.github.osguima3.jooqdsl.core.valueType
import kotlin.reflect.KClass

data class ValueObjectDefinition(override val fromType: KClass<*>, override val toType: KClass<*>) :
    NullableConverterDefinition {

    constructor(toType: KClass<*>) : this(toType.valueType, toType)

    init {
        if (fromType != toType.valueType) {
            throw IllegalArgumentException("$toType.${toType.valueField}(): ${toType.valueType} " +
                "does not match expected type ($fromType).")
        }
    }

    override val from = "${toType.qualified}::new"

    override val to = "${toType.qualified}::${toType.valueField}"
}
