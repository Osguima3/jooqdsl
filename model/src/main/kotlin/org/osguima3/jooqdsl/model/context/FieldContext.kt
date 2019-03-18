package org.osguima3.jooqdsl.model.context

import org.osguima3.jooqdsl.model.converter.Converter
import kotlin.reflect.KClass

interface FieldContext {

    fun <U : Enum<U>> enum(databaseType: String? = null, userType: KClass<U>): FieldDefinition<U>

    fun <U : Any> tinyType(userType: KClass<U>): FieldDefinition<U>

    fun <T : Any, U : Any> tinyType(
        converter: KClass<out Converter<T, *>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ): FieldDefinition<U>

    fun <T : Any, U : Any> custom(
        converter: KClass<out Converter<T, U>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ): FieldDefinition<U>
}

inline fun <reified T : Any, reified U, reified V : Any> FieldContext.tinyType(
    converter: KClass<out Converter<T, U>>,
    userType: KClass<V>
): FieldDefinition<V> = tinyType(converter, T::class, userType)

inline fun <reified T : Any, reified U : Any> FieldContext.custom(
    converter: KClass<out Converter<T, U>>
): FieldDefinition<U> = custom(converter, T::class, U::class)
