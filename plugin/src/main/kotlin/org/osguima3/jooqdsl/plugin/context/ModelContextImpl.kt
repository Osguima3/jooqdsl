/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

package org.osguima3.jooqdsl.plugin.context

import org.jetbrains.kotlin.com.intellij.openapi.util.io.FileUtil
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.osguima3.jooqdsl.model.context.ModelContext
import org.osguima3.jooqdsl.model.context.TablesContext
import org.osguima3.jooqdsl.plugin.converter.TemplateFile
import java.io.File
import java.io.SequenceInputStream
import kotlin.reflect.KClass

class ModelContextImpl(configuration: Configuration) : ModelContext {

    internal val targetPackage = configuration.generator.target.packageName

    internal val converterPackage = "$targetPackage.converters"

    internal val pendingTemplates = mutableSetOf<TemplateFile>()

    private val tablesContext = TablesContextImpl(this)

    private val forcedTypes = configuration.generator.database.forcedTypes

    private val targetDirectory = configuration.generator.target.directory

    private val converterDirectory = converterPackage.replace('.', '/')

    override fun tables(configure: TablesContext.() -> Unit) = tablesContext.configure()

    internal fun registerForcedType(expression: String, userType: KClass<*>, converter: String) {
        forcedTypes += ForcedType().also {
            it.expression = expression
            it.userType = userType.qualifiedName
            it.converter = converter
        }
    }

    internal fun addTemplates(templates: Set<TemplateFile>) {
        pendingTemplates.addAll(templates)
    }

    internal fun generateConverters() = pendingTemplates.map(TemplateFile::className).forEach {
        val source = this::class.java.classLoader.getResource("converter/$it.java")
        val target = File("$targetDirectory/$converterDirectory").run {
            mkdirs()
            File("$absolutePath/$it.java").outputStream()
        }
        println("Generating $converterPackage.$it from $source")
        FileUtil.copy(
            SequenceInputStream("""
                /*
                 * This file is generated by jOOQ DSL.
                 */
                package $converterPackage;


                """.trimIndent().byteInputStream(),
                source.openStream()
            ),
            target
        )
    }
}
