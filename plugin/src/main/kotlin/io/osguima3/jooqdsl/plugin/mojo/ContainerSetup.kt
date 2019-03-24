package io.osguima3.jooqdsl.plugin.mojo

import org.apache.maven.plugin.MojoExecutionException
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Jdbc
import io.osguima3.jooqdsl.plugin.configuration.Container
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.JdbcDatabaseContainerProvider

internal fun startContainer(configuration: Configuration, container: Container, path: String) =
    getContainerProvider(container.provider)
        .newInstance(container.version)
        .withFileSystemBind("$path/${container.migrationPath}", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)
        .apply { bind(configuration, this) }

private fun getContainerProvider(className: String) = Class.forName(className).run {
    if (JdbcDatabaseContainerProvider::class.java.isAssignableFrom(this)) {
        newInstance().let { it as JdbcDatabaseContainerProvider }
    } else {
        throw MojoExecutionException("Container provider class $className not valid, " +
            "must implement ${JdbcDatabaseContainerProvider::class.qualifiedName}"
        )
    }
}

private fun bind(configuration: Configuration, container: JdbcDatabaseContainer<*>) {
    val jdbc = configuration.jdbc ?: Jdbc()
    jdbc.user?.let(container::withUsername)
    jdbc.password?.let(container::withPassword)

    container.start()

    configuration.jdbc = jdbc.apply { url = container.getJdbcUrl() }
}
