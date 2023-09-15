package io.github.osguima3.jooqdsl.plugin.converter

import io.github.osguima3.jooqdsl.model.context.FieldDefinition
import org.jooq.meta.jaxb.ForcedType

interface ForcedTypeDefinition : FieldDefinition {

    val userType: String

    val converter: String

    fun toForcedType(includeExpression: String) = ForcedType().also {
        it.includeExpression = includeExpression
        it.userType = userType
        it.converter = converter
    }
}
