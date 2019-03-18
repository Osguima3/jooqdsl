package org.osguima3.jooqdsl.model.context

import org.osguima3.jooqdsl.model.converter.Converter
import kotlin.reflect.KClass

interface FieldContext {

    fun <T : Enum<T>> enum(fromType: String? = null, toType: KClass<T>): FieldDefinition<T>

    fun <T : Any> tinyType(toType: KClass<T>): FieldDefinition<T>

    fun <T : Any, U, V : Any> tinyType(
        converter: KClass<out Converter<T, U>>,
        fromType: KClass<T>,
        toType: KClass<V>
    ): FieldDefinition<V>

    fun <T : Any, U : Any> custom(
        converter: KClass<out Converter<T, U>>,
        fromType: KClass<T>,
        toType: KClass<U>
    ): FieldDefinition<U>

//    fun <T : Any, U : Any> custom(
//        from: (T) -> U,
//        to: (U) -> T,
//        fromType: KClass<T>,
//        toType: KClass<U>
//    ): FieldDefinition<U>
}

inline fun <reified T : Any, reified U, reified V : Any> FieldContext.tinyType(
    converter: KClass<out Converter<T, U>>,
    toType: KClass<V>
) = tinyType(converter, T::class, toType)

inline fun <reified T : Any, reified U : Any> FieldContext.custom(converter: KClass<out Converter<T, U>>) =
    custom(converter, T::class, U::class)

//inline fun <reified T : Any, reified U : Any> FieldContext.custom(noinline from: (T) -> U, noinline to: (U) -> T) =
//    custom(from, to, T::class, U::class)
