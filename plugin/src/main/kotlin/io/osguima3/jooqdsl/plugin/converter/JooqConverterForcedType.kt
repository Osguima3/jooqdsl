package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.qualified
import org.jooq.Converter
import kotlin.reflect.KClass

data class JooqConverterForcedType(private val converterClass: KClass<out Converter<*, *>>) : IForcedType {

    override val userType = converterClass.constructors
        .single { it.parameters.isEmpty() }.call()
        .toType().canonicalName!!

    override val converter = "new ${converterClass.qualified}()"
}
