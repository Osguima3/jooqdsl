package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.qualified
import org.jooq.Converter
import kotlin.reflect.KClass

data class CustomConverterDefinition(override val converter: String, private val userClass: KClass<*>) :
    ConverterDefinition {

    constructor(converterClass: KClass<out Converter<*, *>>, userClass: KClass<*>) :
        this(converterClass.qualified, userClass)

    override val userType = userClass.qualified
}
