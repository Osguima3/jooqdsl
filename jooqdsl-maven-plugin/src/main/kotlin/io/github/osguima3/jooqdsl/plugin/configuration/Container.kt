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

package io.github.osguima3.jooqdsl.plugin.configuration

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlType
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Jdbc
import org.testcontainers.containers.BindMode.READ_ONLY
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.JdbcDatabaseContainerProvider
import java.io.File
import java.io.Serializable
import java.sql.DriverManager
import java.sql.SQLException

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
            throw IllegalArgumentException("Container provider class ${clazz.canonicalName} not valid, " +
                "must implement ${JdbcDatabaseContainerProvider::class.qualifiedName}"
            )
        }

    private fun JdbcDatabaseContainer<*>.bind(configuration: Configuration) {
        val jdbc = configuration.jdbc ?: Jdbc()
        jdbc.user?.let(::withUsername)
        jdbc.password?.let(::withPassword)

        start()

        // Wait for the database to actually be ready to accept connections
        waitForDatabaseReady(jdbcUrl, username, password)

        configuration.jdbc = jdbc.apply { url = jdbcUrl }
    }

    private fun waitForDatabaseReady(url: String, user: String, password: String, maxAttempts: Int = 10) {
        repeat(maxAttempts) { attempt ->
            try {
                DriverManager.getConnection(url, user, password).use { connection ->
                    if (connection.isValid(5)) {
                        return
                    }
                }
            } catch (e: SQLException) {
                if (attempt < maxAttempts - 1) {
                    Thread.sleep(500)
                } else {
                    throw e
                }
            }
        }
    }
}
