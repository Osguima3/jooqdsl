package org.osguima3.jooqdsl.model

import org.osguima3.jooqdsl.model.context.ModelContext

/**
 * This class is used to define the model to be used in generated Jooq classes.
 */
class ModelDefinition(val configure: ModelContext.() -> Unit)
