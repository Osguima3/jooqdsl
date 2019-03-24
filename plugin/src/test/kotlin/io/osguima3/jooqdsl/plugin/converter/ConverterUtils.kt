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

package io.osguima3.jooqdsl.plugin.converter

import org.jooq.tools.reflect.Reflect

typealias JooqConverter<T, U> = org.jooq.Converter<T, U>
typealias JavaFunction<T, U> = java.util.function.Function<T, U>

fun <T, U> Any.loadConverter(template: TemplateFile, vararg args: Any): JooqConverter<T, U> {
    val classLoader = this::class.java.classLoader
    return Reflect.compile(
        template.className,
        classLoader
            .getResourceAsStream("converter/${template.className}.java")
            .reader().use { it.readText() }
    ).create(*args)
        .get()
}

val <T, U> Function1<T, U>.java
    get() = JavaFunction<T, U> { this(it) }
