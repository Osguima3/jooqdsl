package org.osguima3.jooqdsl.plugin

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.jooq.codegen.GenerationTool
import org.jooq.codegen.GenerationTool.DEFAULT_TARGET_DIRECTORY
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import org.osguima3.jooqdsl.model.ModelDefinition
import org.osguima3.jooqdsl.plugin.container.PostgresContainer
import org.osguima3.jooqdsl.plugin.context.ModelContextImpl
import org.testcontainers.containers.BindMode
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

    @Parameter
    private lateinit var postgresContainerImage: String

    @Parameter
    private lateinit var migrationPath: String

    override fun execute() {
        val path = project.basedir.absolutePath
        val classRealm = descriptor.classRealm

        project.runtimeClasspathElements
            .map(::File)
            .map(File::toURI)
            .map(URI::toURL)
            .forEach(classRealm::addURL)

        val definition: ModelDefinition = FileLoader()
            .loadScript("$path/src/main/resources/db/definition/model_definition.kts")

        val postgresContainer = PostgresContainer(postgresContainerImage)
            .withFileSystemBind("$path/$migrationPath", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)
            .apply { jdbc?.let { withUsername(it.user).withPassword(it.password) } }
            .apply { start() }

        val configuration = Configuration().also {
            it.logging = logging
            it.jdbc = (jdbc ?: Jdbc()).apply { url = postgresContainer.jdbcUrl }
            it.generator = generator.apply {
                target.directory = "$path/${target.directory ?: DEFAULT_TARGET_DIRECTORY}"
            }
        }

        ModelContextImpl(configuration).run(definition.configure)

        GenerationTool().run(configuration)
    }
}
