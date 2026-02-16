package io.github.osguima3.jooqdsl.core.context

import io.github.osguima3.jooqdsl.core.converter.FieldDefinition

interface JooqContext {

    fun configureField(tableName: String, fieldName: String, definition: FieldDefinition)
}
