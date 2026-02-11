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

package io.github.osguima3.jooqdsl.core.context

import io.github.osguima3.jooqdsl.model.context.FieldDefinition
import io.github.osguima3.jooqdsl.model.context.FieldContext
import io.github.osguima3.jooqdsl.model.context.TableContext
import io.github.osguima3.jooqdsl.core.converter.ForcedTypeDefinition
import io.github.osguima3.jooqdsl.core.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.core.qualified
import kotlin.reflect.KClass

class TableContextImpl(private val context: JooqContext, private val tableName: String) : TableContext {

    private val fields = mutableSetOf<String>()

    override fun field(name: String, type: KClass<*>) = field(name) { type(type) }

    override fun field(name: String, configure: FieldContext.() -> FieldDefinition) = when {
        fields.add(name) -> with(FieldContextImpl(context.targetPackage)) { register(name, configure()) }
        else -> throw IllegalArgumentException("Field $name already initialized")
    }

    private fun register(name: String, definition: FieldDefinition) = when (definition) {
        is SkippedDefinition -> Unit
        is ForcedTypeDefinition -> context.registerForcedType(definition.toForcedType(".*\\.$tableName\\.$name"))
        else -> throw IllegalArgumentException("Unexpected definition of type ${definition::class::qualified}")
    }
}
