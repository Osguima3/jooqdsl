package org.osguima3.jooqdsl.plugin.context

import org.osguima3.jooqdsl.model.context.FieldContext
import org.osguima3.jooqdsl.model.context.FieldDefinition
import org.osguima3.jooqdsl.model.context.TableContext
import kotlin.reflect.KClass

class TableContextImpl(private val context: ModelContextImpl, private val tableName: String) : TableContext {

    /**
     * A generic converter will be generated for this field, where a database type field is simply
     * encapsulated in the tiny type.
     */
    override fun <T : Any> field(name: String, type: KClass<T>) = field(name) {
        (this as FieldContextImpl).resolve(type)
    }

    override fun <T> field(name: String, configure: FieldContext.() -> FieldDefinition<T>) {
        FieldContextImpl(context, tableName, name).configure()
    }
}
