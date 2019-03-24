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

package org.osguima3.jooqdsl.plugin.mojo

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.jooq.codegen.GenerationTool.DEFAULT_TARGET_DIRECTORY
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import org.osguima3.jooqdsl.model.ModelDefinition
import org.osguima3.jooqdsl.plugin.configuration.Container
import org.osguima3.jooqdsl.plugin.context.ModelContextImpl
import java.io.File
import java.net.URI

@Mojo(
    name = "generate-jooq", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.TEST
)
class JooqDslGenerateMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", readonly = true)
    private lateinit var project: MavenProject

    @Parameter(defaultValue = "\${plugin}", readonly = true)
    private lateinit var descriptor: PluginDescriptor

    @Parameter(required = false)
    private var logging: Logging? = null

    @Parameter(required = false)
    private var jdbc: Jdbc? = null

    @Parameter(required = true)
    private lateinit var generator: Generator

    @Parameter(required = false)
    private var container: Container? = null

    @Parameter(defaultValue = "src/main/resources/db/definition/model_definition.kts")
    private lateinit var definitionFile: String

    override fun execute() {
        val path = project.basedir.absolutePath
        val classRealm = descriptor.classRealm

        project.runtimeClasspathElements
            .map(::File)
            .map(File::toURI)
            .map(URI::toURL)
            .forEach(classRealm::addURL)

        log.info("Loading jOOQ model definition from $path/$definitionFile")
        val modelDefinition: ModelDefinition = ScriptLoader()
            .loadScript("$path/$definitionFile")

        val configuration = Configuration().also {
            it.logging = logging
            it.jdbc = jdbc
            it.generator = generator.apply {
                target.directory = "$path/${target.directory ?: DEFAULT_TARGET_DIRECTORY}"
            }
        }

        container?.apply {
            if (jdbc?.url != null) {
                log.warn("Provided jdbc url (${jdbc?.url}) will be replaced with container generated one")
            }

            startContainer(configuration, this, path)
        }

        ModelContextImpl(configuration).generate(modelDefinition.configure)
    }
}
