package io.osguima3.jooqdsl.plugin.context

import io.osguima3.jooqdsl.plugin.converter.IForcedType

interface JooqContext {

    val targetPackage: String

    fun registerForcedType(expression: String, forcedType: IForcedType)
}
