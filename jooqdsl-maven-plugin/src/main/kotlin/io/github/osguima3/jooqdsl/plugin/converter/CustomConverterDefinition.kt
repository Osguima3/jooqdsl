package io.github.osguima3.jooqdsl.plugin.converter

import io.github.osguima3.jooqdsl.plugin.qualified
import org.jooq.Converter
import kotlin.reflect.KClass

data class CustomConverterDefinition(override val converter: String, private val userClass: KClass<*>) :
    ForcedTypeDefinition {

    constructor(converterClass: KClass<out Converter<*, *>>, userClass: KClass<*>) :
        this(converterClass.qualified, userClass)

    override val userType = userClass.qualified
}
