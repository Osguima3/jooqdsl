package io.github.osguima3.jooqdsl.plugin.converter

import org.jooq.meta.jaxb.ForcedType

interface ConverterDefinition {

    val userType: String

    val converter: String

    fun toForcedType(includeExpression: String) = ForcedType().also {
        it.includeExpression = includeExpression
        it.userType = userType
        it.converter = converter
    }
}
