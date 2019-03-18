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

package org.jooq.codegen

import org.jooq.meta.Definition

class ConverterGenerator : JavaGenerator() {

    fun generateConverter(targetPackage: String, className: String) {
        val definition: Definition = ConverterDefinition()
        val out = newJavaWriter(getFile(definition))
        generateConverter(out, definition)
    }

    private fun generateConverter(out: JavaWriter, definition: Definition) {
        val schema = definition.schema

        printPackage(out, definition)

        printClassAnnotations(out, schema)

        closeJavaWriter(out)
    }

    class ConverterDefinition : Definition {
        override fun getOutputName() = TODO("not implemented")
        override fun getComment() = TODO("not implemented")
        override fun getName() = TODO("not implemented")
        override fun getQualifiedOutputName() = TODO("not implemented")
        override fun getDatabase() = TODO("not implemented")
        override fun getInputName() = TODO("not implemented")
        override fun getQualifiedInputName() = TODO("not implemented")
        override fun getQualifiedName() = TODO("not implemented")
        override fun getQualifiedNamePart() = TODO("not implemented")
        override fun getQualifiedOutputNamePart() = TODO("not implemented")
        override fun getOverload() = TODO("not implemented")
        override fun getCatalog() = TODO("not implemented")
        override fun getSchema() = TODO("not implemented")
        override fun getDefinitionPath() = TODO("not implemented")
        override fun getQualifiedInputNamePart() = TODO("not implemented")
    }
}