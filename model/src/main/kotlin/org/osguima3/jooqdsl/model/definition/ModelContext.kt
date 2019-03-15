package org.osguima3.jooqdsl.model.definition

abstract class ModelContext {

    fun tables(block: TablesContext.() -> Unit) {
        tablesContext.block()
    }

    protected abstract val tablesContext: TablesContext
}
