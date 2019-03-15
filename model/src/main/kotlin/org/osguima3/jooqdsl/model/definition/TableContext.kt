package org.osguima3.jooqdsl.model.definition

import org.osguima3.jooqdsl.model.converter.Converter
import kotlin.reflect.KClass

interface TableContext {

    /**
     * A generic converter will be generated for this field, where a database type field is simply
     * encapsulated in the tiny type.
     */
    infix fun String.withTinyType(tinyType: KClass<*>)

    infix fun String.withInstantTinyType(tinyType: KClass<*>)

    infix fun String.withEnum(enum: KClass<out Enum<*>>)

    infix fun String.withStringEnum(enum: KClass<out Enum<*>>)

    /**
     * The indicated converter will be used for this field. Used for fields where any conversion is needed
     * between the database type and the user type that is not covered in the other generators
     */
    infix fun <T : Any, U : Any> String.withCustomConverter(converterClass: KClass<out Converter<T, U>>):
        ConverterContext<T, U>
}

inline fun <reified T : Any, reified U : Any> TableContext.withCustomConverter(
    name: String,
    converterClass: KClass<out Converter<T, U>>
) = name withCustomConverter converterClass from T::class to U::class
