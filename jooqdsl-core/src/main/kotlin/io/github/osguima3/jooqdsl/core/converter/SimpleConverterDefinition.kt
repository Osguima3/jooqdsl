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

import io.github.osguima3.jooqdsl.core.isObjectType
import io.github.osguima3.jooqdsl.core.qualified
import kotlin.reflect.KClass

internal const val INSTANCE_FIELD = "INSTANCE"

data class SimpleConverterDefinition(
    override val fromType: KClass<*>,
    override val toType: KClass<*>,
    private val converterClass: KClass<*>
) : NullableConverterDefinition {

    init {
        if (!converterClass.isObjectType) {
            throw IllegalArgumentException("Converter $converterClass should be a kotlin `object` " +
                "or have an $INSTANCE_FIELD singleton field.")
        }
    }

    override val from = "${converterClass.qualified}.$INSTANCE_FIELD::from"

    override val to = "${converterClass.qualified}.$INSTANCE_FIELD::to"
}
