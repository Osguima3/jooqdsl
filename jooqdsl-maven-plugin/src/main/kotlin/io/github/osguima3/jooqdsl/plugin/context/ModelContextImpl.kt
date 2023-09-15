/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 */

package io.github.osguima3.jooqdsl.plugin.context

import io.github.osguima3.jooqdsl.model.context.ModelContext
import io.github.osguima3.jooqdsl.model.context.TablesContext
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generator

class ModelContextImpl(override val targetPackage: String, private val forcedTypes: MutableList<ForcedType>) : ModelContext, JooqContext {

    constructor(generator: Generator) : this(generator.target.packageName, generator.database.forcedTypes)

    private val tablesContext = TablesContextImpl(this)

    override fun tables(configure: TablesContext.() -> Unit) = tablesContext.configure()

    override fun registerForcedType(forcedType: ForcedType) {
        forcedTypes += forcedType
    }

    internal fun generate(configure: ModelContext.() -> Unit) {
        configure()
    }
}
