package org.osguima3.jooqdsl.model.context

import kotlin.reflect.KClass

interface TableContext {

    /**
     * Map this database field to a specific user type using the default converter.
     *
     * Default converters will be provided for the following types:
     * * Platform-supported fields (e.g. `String` is mapped to `varchar`-like types).
     * * Tiny types (e.g. `MyTinyType(val value: String)` is mapped to `varchar`).
     * * Enums (e.g. `MyEnum` is mapped to `my_custom_db_enum_type`).
     *
     * @param name the database field's name
     * @param type the user type used to represent this database field
     */
    fun <T : Any> field(name: String, type: KClass<T>)

    /**
     * Map this database field to a specific user type using a custom converter.
     *
     * @param name the database field's name
     * @param configure used to map between database and user types
     */
    fun <T> field(name: String, configure: FieldContext.() -> FieldDefinition<T>)
}
