package org.osguima3.jooqdsl.plugin.visitor

import org.jooq.meta.jaxb.Configuration
import org.osguima3.jooqdsl.model.definition.TableContext
import org.osguima3.jooqdsl.model.definition.TablesContext

class TablesContextImpl(private val configuration: Configuration) : TablesContext {

    private val tables = mapOf<String, TableContext>()

    override fun tableContext(name: String) = tables.getOrElse(name) { TableContextImpl(configuration, name) }
}
