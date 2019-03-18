package org.osguima3.jooqdsl.plugin.converter

import org.jooq.tools.reflect.Reflect
import org.osguima3.jooqdsl.model.converter.Converter

typealias JooqConverter<T, U> = org.jooq.Converter<T, U>
typealias JavaFunction<T, U> = java.util.function.Function<T, U>

fun <T, U> Any.loadConverter(name: String, vararg args: Any): JooqConverter<T, U> = Reflect
    .compile(
        "${Converter::class.java.`package`.name}.$name",
        this::class.java.classLoader.getResourceAsStream("converter/$name.java").reader().use { it.readText() }
    )
    .create(*args)
    .get()

val <T, U> Function1<T, U>.java
    get() = JavaFunction<T, U> { this(it) }