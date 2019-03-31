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

package io.osguima3.jooqdsl.model.context

import kotlin.reflect.KClass

/**
 * Provides the DSL context for a jOOQ table.
 */
interface TableContext {

    /**
     * Map this database field to a specific user type using the default converter.
     *
     * Default converters will be provided for the following types:
     * * Platform-supported fields (e.g. `String` is mapped to `varchar`-like types).
     * * Enums (e.g. `MyEnum` is mapped to `my_custom_db_enum_type`).
     * * Value objects (e.g. `MyValueObject(val value: String)` is mapped to `varchar`).
     *
     * @param name The database field's name
     * @param type The user type used to represent this field
     */
    fun field(name: String, type: KClass<*>)

    /**
     * Map this database field to a specific user type using a custom converter.
     *
     * @param name The database field's name
     * @param configure Field configuration block
     */
    fun field(name: String, configure: FieldContext.() -> Unit)
}
