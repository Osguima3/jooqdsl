package org.osguima3.jooqdsl.model

import org.osguima3.jooqdsl.model.definition.ModelContext

/**
 * This class is used to define the model to be used in generated Jooq classes.
 */
open class ModelDefinition(private val configure: ModelContext.() -> Unit) {

    fun accept(visitor: DefinitionVisitor) = visitor.visit(configure)
}
