package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.qualified
import io.osguima3.jooqdsl.plugin.valueField
import io.osguima3.jooqdsl.plugin.valueType
import kotlin.reflect.KClass

data class ValueObjectForcedType(override val fromType: KClass<*>, override val toType: KClass<*>) : SimpleForcedType {

    constructor(toType: KClass<*>) : this(toType.valueType, toType)

    init {
        if (fromType != toType.valueType) {
            throw IllegalArgumentException("$toType.${toType.valueField}(): ${toType.valueType} " +
                "does not match expected type ($fromType).")
        }
    }

    override val from = "${toType.qualified}::new"

    override val to = "${toType.qualified}::${toType.valueField}"
}
