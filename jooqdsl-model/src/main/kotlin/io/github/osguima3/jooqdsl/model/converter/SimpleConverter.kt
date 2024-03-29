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

package io.github.osguima3.jooqdsl.model.converter

/**
 * A simple [Converter] using two expressions to convert between user and database types.
 *
 * @param <T> The database type - i.e. any type available from [org.jooq.impl.SQLDataType]
 * @param <U> The user type
 * @param from the expression used to convert from database type to user type
 * @param to the expression used to convert from user type to database type
 */
open class SimpleConverter<T, U>(private val from: (T) -> U, private val to: (U) -> T) : Converter<T, U> {

    /**
     * Convert a database object to a user object
     *
     * @param databaseObject The database object
     * @return The user object
     */
    override fun from(databaseObject: T) = from.invoke(databaseObject)

    /**
     * Convert a user object to a database object
     *
     * @param userObject The user object
     * @return The database object
     */
    override fun to(userObject: U) = to.invoke(userObject)
}
