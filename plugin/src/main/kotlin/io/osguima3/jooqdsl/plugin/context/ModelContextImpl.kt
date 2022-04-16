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

package io.osguima3.jooqdsl.plugin.context

import io.osguima3.jooqdsl.model.context.ModelContext
import io.osguima3.jooqdsl.model.context.TablesContext
import io.osguima3.jooqdsl.plugin.converter.IForcedType
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType

class ModelContextImpl(private val configuration: Configuration) : ModelContext, JooqContext {

    private val tablesContext = TablesContextImpl(this)

    private val forcedTypes = configuration.generator.database.forcedTypes

    override val targetPackage: String = configuration.generator.target.packageName

    override fun tables(configure: TablesContext.() -> Unit) = tablesContext.configure()

    override fun registerForcedType(expression: String, forcedType: IForcedType) {
        forcedTypes += ForcedType().also {
            it.expression = expression
            it.userType = forcedType.userType
            it.converter = forcedType.converter
        }
    }

    internal fun generate(configure: ModelContext.() -> Unit) {
        configure()
        GenerationTool().run(configuration)
    }
}
