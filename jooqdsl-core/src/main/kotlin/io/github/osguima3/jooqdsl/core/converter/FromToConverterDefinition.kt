package io.github.osguima3.jooqdsl.core.converter

import kotlin.reflect.KClass

open class FromToConverterDefinition(
    override val fromType: KClass<*>,
    override val toType: KClass<*>,
    override val from: String,
    override val to: String
) : NullableConverterDefinition
