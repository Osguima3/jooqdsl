package org.osguima3.jooqdsl.plugin.context

import org.osguima3.jooqdsl.model.context.TableContext
import org.osguima3.jooqdsl.model.context.TablesContext

class TablesContextImpl(private val context: ModelContextImpl) : TablesContext {

    private val tables = mutableMapOf<String, TableContext>()

    override fun table(name: String, block: TableContext.() -> Unit) = tables
        .getOrPut(name) { TableContextImpl(context, name) }
        .block()
}
