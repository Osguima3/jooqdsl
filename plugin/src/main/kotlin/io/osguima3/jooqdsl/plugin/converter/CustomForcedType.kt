package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.qualified
import kotlin.reflect.KClass

data class CustomForcedType(override val converter: String, private val userClass: KClass<*>) : IForcedType {

    override val userType = userClass.qualified
}
