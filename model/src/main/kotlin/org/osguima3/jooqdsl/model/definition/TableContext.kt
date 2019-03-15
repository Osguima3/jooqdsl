package org.osguima3.jooqdsl.model.definition

import org.osguima3.jooqdsl.model.converter.Converter
import kotlin.reflect.KClass

interface TableContext {

    /**
     * Map this database field to a user tiny type wrapping the converted database type.
     *
     * e.g. `varchar` is mapped to a `MyTinyType(val value: String)`.
     *
     * @param tinyType the user tiny type
     */
    infix fun String.withTinyType(tinyType: KClass<*>)

    /**
     * Map this database enum field to a user enum type.
     *
     * e.g. `my_custom_db_enum_type` is mapped to `MyEnum`
     *
     * @param enum the user enum
     */
    infix fun String.withEnum(enum: KClass<out Enum<*>>)

    /**
     * Map this database string field to a user enum type.
     *
     * e.g. `varchar` is mapped to `MyEnum`
     *
     * @param enum the user enum
     */
    infix fun String.withStringEnum(enum: KClass<out Enum<*>>)

    /**
     * Map this database field to a user type using the indicated custom converter.
     *
     * This overload requires indicating the `from` and `to` types.
     *
     * @param converter the custom converter
     */
    infix fun <T : Any, U : Any> String.withCustomConverter(converter: KClass<out Converter<T, U>>):
        ConverterContext<T, U>
}

/**
 * Map this database field to a user type using the indicated custom converter.
 *
 * @param converter the custom converter
 */
inline fun <reified T : Any, reified U : Any> TableContext.withCustomConverter(
    name: String,
    converter: KClass<out Converter<T, U>>
) = name withCustomConverter converter from T::class to U::class
