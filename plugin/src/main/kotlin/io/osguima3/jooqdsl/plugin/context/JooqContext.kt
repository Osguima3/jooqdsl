package io.osguima3.jooqdsl.plugin.context

import kotlin.reflect.KClass

interface JooqContext {

    val targetPackage: String

    fun registerForcedType(expression: String, userType: KClass<*>, converter: String)
}
