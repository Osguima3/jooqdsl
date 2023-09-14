package io.github.osguima3.jooqdsl.plugin.context

import org.jooq.meta.jaxb.ForcedType

interface JooqContext {

    val targetPackage: String

    fun registerForcedType(forcedType: ForcedType)
}
