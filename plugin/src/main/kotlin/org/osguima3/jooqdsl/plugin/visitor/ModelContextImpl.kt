package org.osguima3.jooqdsl.plugin.visitor

import org.jooq.meta.jaxb.Configuration
import org.osguima3.jooqdsl.model.definition.ModelContext

class ModelContextImpl(configuration: Configuration) : ModelContext() {

    override val tablesContext = TablesContextImpl(configuration)
}
