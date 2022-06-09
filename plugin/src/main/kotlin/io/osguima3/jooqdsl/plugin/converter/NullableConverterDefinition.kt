package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.qualified
import kotlin.reflect.KClass

interface NullableConverterDefinition : ConverterDefinition {

    val toType: KClass<*>

    val fromType: KClass<*>

    val from: String

    val to: String

    override val userType
        get() = toType.qualified

    override val converter
        get() = "org.jooq.Converter.ofNullable(${fromType.qualified}.class, $userType.class, $from, $to)"
}
