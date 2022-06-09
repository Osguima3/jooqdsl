package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.isObjectType
import io.osguima3.jooqdsl.plugin.qualified
import kotlin.reflect.KClass

internal const val INSTANCE_FIELD = "INSTANCE"

data class SimpleConverterDefinition(
    override val fromType: KClass<*>,
    override val toType: KClass<*>,
    private val converterClass: KClass<*>
) : NullableConverterDefinition {

    init {
        if (!converterClass.isObjectType) {
            throw IllegalArgumentException("Converter $converterClass should be a kotlin `object` " +
                "or have an $INSTANCE_FIELD singleton field.")
        }
    }

    override val from = "${converterClass.qualified}.$INSTANCE_FIELD::from"

    override val to = "${converterClass.qualified}.$INSTANCE_FIELD::to"
}
