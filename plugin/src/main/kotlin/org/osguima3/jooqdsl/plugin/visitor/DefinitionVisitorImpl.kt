package org.osguima3.jooqdsl.plugin.visitor

import org.jooq.meta.jaxb.Configuration
import org.osguima3.jooqdsl.model.DefinitionVisitor
import org.osguima3.jooqdsl.model.definition.ModelContext

class DefinitionVisitorImpl(configuration: Configuration) : DefinitionVisitor {

    private val modelContext = ModelContextImpl(configuration)

    override fun visit(configure: ModelContext.() -> Unit) {
        modelContext.configure()
    }
}
