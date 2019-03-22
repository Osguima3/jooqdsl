/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

package org.osguima3.jooqdsl.model.context

import org.osguima3.jooqdsl.model.converter.Converter
import kotlin.reflect.KClass

/**
 * Maps this field to the specified user tiny type using the specified converter to map
 * the child field to the database type.
 * @param converter The converter used to map between the child field and the database type
 * @param userType The tiny type class
 */
inline fun <reified T : Any, reified U : Any> FieldContext.tinyType(
    converter: KClass<out Converter<T, U>>,
    userType: KClass<*>
) = tinyType(converter, userType, T::class, U::class)

/**
 * Maps this field to the specified user type using a custom converter.
 * @param converter The converter used to map between the user type and the database type
 */
inline fun <reified T : Any, reified U : Any> FieldContext.custom(
    converter: KClass<out Converter<T, U>>
) = custom(converter, U::class, T::class)
