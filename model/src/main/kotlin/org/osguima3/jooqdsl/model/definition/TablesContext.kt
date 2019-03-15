package org.osguima3.jooqdsl.model.definition

interface TablesContext {

    fun tableContext(name: String): TableContext

    fun table(name: String, block: TableContext.() -> Unit) {
        tableContext(name).block()
    }
}
