package org.osguima3.jooqdsl.model.definition

import org.osguima3.jooqdsl.model.DefinitionVisitor

class TestDefinitionVisitor(tableContext: (String) -> TableContext) : DefinitionVisitor {

    private val testTablesContext = object : TablesContext {
        override fun tableContext(name: String) = tableContext(name)
    }

    private val modelContext = object : ModelContext() {
        override val tablesContext = testTablesContext
    }

    override fun visit(configure: ModelContext.() -> Unit) = modelContext.configure()
}
