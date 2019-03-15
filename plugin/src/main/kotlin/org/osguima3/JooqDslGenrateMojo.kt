package org.osguima3

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.testcontainers.containers.BindMode
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths


@Mojo(
    name = "generate-jooq", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.TEST
    //,threadSafe = true

)
class JooqDslGenrateMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", readonly = true)
    private lateinit var mavenProject: MavenProject

    @Parameter(required = true)
    lateinit var jdbc: org.jooq.meta.jaxb.Jdbc

    @Parameter(required = true)
    lateinit var generator: org.jooq.meta.jaxb.Generator

    @Parameter(defaultValue = "\${plugin}", readonly = true)
    private lateinit var descriptor: PluginDescriptor

    @Parameter
    private lateinit var databaseName: String

    @Parameter
    private lateinit var postgresContainerImage: String

    override fun execute() {

        val oldCL = Thread.currentThread().contextClassLoader
        val pluginClassLoader = getClassLoader()

        try {
            Thread.currentThread().contextClassLoader = pluginClassLoader
            val postgresContainer = PostgresContainer(postgresContainerImage)
                .withDatabaseName(databaseName)
                .withUsername(jdbc.user)
                .withPassword(jdbc.password)
                .withFileSystemBind(
                    Paths.get("src/main/resources/db/migration").toAbsolutePath().toString(),
                    "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY
                )
            postgresContainer.start()
            val configuration = Configuration()
            jdbc.withUrl(postgresContainer.jdbcUrl)
            configuration.jdbc = jdbc
            configuration.generator = generator

            GenerationTool().run(configuration)
        } finally {
            Thread.currentThread().contextClassLoader = oldCL
            pluginClassLoader.close()
        }
    }

    @Throws(MojoExecutionException::class)
    private fun getClassLoader(): URLClassLoader {
        try {
            val classpathElements = mavenProject.runtimeClasspathElements
            mavenProject.build.resources.forEach {
                classpathElements.add(it.directory)
            }
            val urls = arrayOfNulls<URL>(classpathElements.size)
            for (i in urls.indices) {
                urls[i] = File(classpathElements[i]).toURI().toURL()
            }

            return URLClassLoader(urls, javaClass.classLoader)
        } catch (e: Exception) {
            throw MojoExecutionException("Couldn't create a classloader.", e)
        }
    }
}
