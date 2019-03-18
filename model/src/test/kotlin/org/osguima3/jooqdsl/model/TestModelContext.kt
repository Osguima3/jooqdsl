package org.osguima3.jooqdsl.model

import org.osguima3.jooqdsl.model.context.ModelContext
import org.osguima3.jooqdsl.model.context.TableContext
import org.osguima3.jooqdsl.model.context.TablesContext

class TestModelContext(tableContext: (String) -> TableContext) : ModelContext {

    private val tablesContext = object : TablesContext {

        override fun table(name: String, block: TableContext.() -> Unit) = tableContext(name).block()
    }

    override fun tables(block: TablesContext.() -> Unit) = tablesContext.block()
}
