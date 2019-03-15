package org.osguima3.jooqdsl.model

import org.osguima3.jooqdsl.model.definition.ModelContext

interface DefinitionVisitor {

    fun visit(configure: ModelContext.() -> Unit)
}
