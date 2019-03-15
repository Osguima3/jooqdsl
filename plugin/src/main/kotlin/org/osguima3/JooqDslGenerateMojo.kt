package org.osguima3

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.osguima3.jooqdsl.model.ModelDefinition
import org.osguima3.jooqdsl.plugin.visitor.DefinitionVisitorImpl
import org.testcontainers.containers.BindMode
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

@Mojo(
    name = "generate-jooq", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.TEST
)
class JooqDslGenerateMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", readonly = true)
    private lateinit var mavenProject: MavenProject

    @Parameter(defaultValue = "\${plugin}", readonly = true)
    private lateinit var descriptor: PluginDescriptor

    @Parameter(required = true)
    private lateinit var jdbc: Jdbc

    @Parameter(required = true)
    private lateinit var generator: Generator

    @Parameter
    private lateinit var postgresContainerImage: String

    @Parameter
    private lateinit var migrationPath: String

    override fun execute() {

        val path = mavenProject.basedir.absolutePath
        val classRealm = descriptor.classRealm

        mavenProject.runtimeClasspathElements
            .map(::File)
            .map(File::toURI)
            .map(URI::toURL)
            .forEach(classRealm::addURL)

        val scriptReader = Files.newBufferedReader(Paths.get("$path/src/main/resources/db/model_definition.kts"))
        val loadedModelDefinition: ModelDefinition = KtsObjectLoader().load(scriptReader)

        log.info("Absolute path: $path/$migrationPath")
        val postgresContainer = PostgresContainer(postgresContainerImage)
            .withUsername(jdbc.user)
            .withPassword(jdbc.password)
            .withFileSystemBind("$path/$migrationPath", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)
            .apply { start() }

        val configuration = Configuration().also {
            it.jdbc = jdbc.apply { url = postgresContainer.jdbcUrl }
            it.generator = generator.apply { target.directory = "$path/${target.directory}" }
        }

        loadedModelDefinition.accept(DefinitionVisitorImpl(configuration))

        GenerationTool().run(configuration)
    }
}