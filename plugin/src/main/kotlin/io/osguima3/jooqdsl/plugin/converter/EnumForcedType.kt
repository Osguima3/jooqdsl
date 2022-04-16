package io.osguima3.jooqdsl.plugin.converter

import io.osguima3.jooqdsl.plugin.context.JooqContext
import io.osguima3.jooqdsl.plugin.qualified
import io.osguima3.jooqdsl.plugin.simple
import kotlin.reflect.KClass

data class EnumForcedType(private val fromClass: String, private val toType: KClass<*>) : IForcedType {

    constructor(context: JooqContext, toClass: KClass<*>) :
        this("${context.targetPackage}.enums.${toClass.simple}", toClass)

    override val userType = toType.qualified

    override val converter = "new org.jooq.impl.EnumConverter<>($fromClass.class, $userType.class)"
}
