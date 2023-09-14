package io.github.osguima3.jooqdsl.plugin.configuration

import org.apache.maven.plugin.MojoExecutionException
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Jdbc
import org.testcontainers.containers.BindMode.READ_ONLY
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.JdbcDatabaseContainerProvider
import java.io.File
import java.io.Serializable
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

private const val CONTAINER_PATH = "/docker-entrypoint-initdb.d/"

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Container")
class Container : Serializable {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter::class)
    internal lateinit var provider: String

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter::class)
    private lateinit var version: String

    @XmlJavaTypeAdapter(NormalizedStringAdapter::class)
    private var initScript: String? = null

    @XmlJavaTypeAdapter(NormalizedStringAdapter::class)
    private var migrationPath: String? = null

    internal fun startContainer(configuration: Configuration, path: File) =
        getContainerProvider(Class.forName(provider)).newInstance(version).apply {
            initScript?.let { withInitScript("${path.absolutePath}/$it") }
            migrationPath?.let { withFileSystemBind("${path.absolutePath}/$it", CONTAINER_PATH, READ_ONLY) }
            bind(configuration)
        }

    private fun getContainerProvider(clazz: Class<*>) =
        if (JdbcDatabaseContainerProvider::class.java.isAssignableFrom(clazz)) {
            clazz.getConstructor().newInstance() as JdbcDatabaseContainerProvider
        } else {
            throw MojoExecutionException("Container provider class ${clazz.canonicalName} not valid, " +
                "must implement ${JdbcDatabaseContainerProvider::class.qualifiedName}"
            )
        }

    private fun JdbcDatabaseContainer<*>.bind(configuration: Configuration) {
        val jdbc = configuration.jdbc ?: Jdbc()
        jdbc.user?.let(::withUsername)
        jdbc.password?.let(::withPassword)

        start()

        configuration.jdbc = jdbc.apply { url = jdbcUrl }
    }
}
