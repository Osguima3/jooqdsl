package org.osguima3.jooqdsl.model.context

interface TablesContext {

    fun table(name: String, block: TableContext.() -> Unit)
}
