/*
 * Note: Based on org.jooq.Converter
 * http://www.jooq.org/javadoc/3.11.9/org/jooq/Converter.html
 *
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

package io.osguima3.jooqdsl.model.converter

/**
 * A [Converter] for data types.
 *
 * @see org.jooq.Converter
 * @param <T> The database type - i.e. any type available from [org.jooq.impl.SQLDataTypeSQLDataType]
 * @param <U> The user type
 */
interface Converter<T, U> {

    /**
     * Convert a database object to a user object
     *
     * @param databaseObject The database object
     * @return The user object
     */
    fun from(databaseObject: T): U

    /**
     * Convert a user object to a database object
     *
     * @param userObject The user object
     * @return The database object
     */
    fun to(userObject: U): T
}
