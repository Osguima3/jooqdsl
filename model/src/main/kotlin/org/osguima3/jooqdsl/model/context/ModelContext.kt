package org.osguima3.jooqdsl.model.context

interface ModelContext {

    fun tables(block: TablesContext.() -> Unit)
}
