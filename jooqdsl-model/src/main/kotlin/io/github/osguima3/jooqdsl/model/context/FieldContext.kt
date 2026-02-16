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

package io.github.osguima3.jooqdsl.model.context

import io.github.osguima3.jooqdsl.model.JooqConverter
import io.github.osguima3.jooqdsl.model.converter.Converter
import kotlin.reflect.KClass

/**
 * Provides the DSL context for a jOOQ field.
 */
interface FieldContext {

    /**
     * Maps this field to the specified user type using a default converter.
     * @param userType The type of the generated class field
     */
    fun type(userType: KClass<*>)

    /**
     * Maps this field to the specified user enum using a default converter.
     * @param userType The type of the generated class field
     * @param databaseType The type of the database field. If not defined, a type with the same name as the user type
     * will be used
     */
    fun enum(userType: KClass<out Enum<*>>, databaseType: String? = null)

    /**
     * Maps this field to the specified user value object using the specified converter to map
     * the child field to the database type.
     * @param converter The converter used to map between the child field and the database type
     * @param userType The value object class
     * @param databaseType The database type, not required when using the inline overload
     * @param valueType The child field type, not required when using the inline overload
     */
    fun <T : Any, U : Any> valueObject(
        converter: KClass<out Converter<T, U>>,
        userType: KClass<*>,
        databaseType: KClass<T>,
        valueType: KClass<U>
    )

    /**
     * Maps this field to the specified user type using a custom converter.
     * @param converter The converter used to map between the user type and the database type
     * @param userType The user type, not required when using the inline overload
     * @param databaseType The database type, not required when using the inline overload
     */
    fun <T : Any, U : Any> converter(
        converter: KClass<out Converter<T, U>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    )

    /**
     * Maps this field to the specified user type using a custom jOOQ converter.
     * @param converter The converter used to map between the user type and the database type
     * @param userType The user type, not required when using the inline overload
     */
    fun <U : Any> converter(converter: KClass<out JooqConverter<*, U>>, userType: KClass<U>)

    /**
     * Maps this field to the specified user type using a custom jOOQ converter.
     * @param converter The converter used to map between the user type and the database type
     * @param userType The user type
     */
    fun custom(userType: KClass<*>, converter: String)
}
