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

package io.github.osguima3.jooqdsl.it.multimodule.app.repository

import io.github.osguima3.jooqdsl.it.multimodule.model.types.CustomEnum
import io.github.osguima3.jooqdsl.it.multimodule.model.types.DateValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.InstantValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.StringEnum
import io.github.osguima3.jooqdsl.it.multimodule.model.types.StringValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.TestClass
import org.jooq.impl.DSL.using
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.MountableFile
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class TestRepositoryTest {

    private val migrationPath = this::class.java.getResource("/db/migration")!!.path

    private var postgresContainer = PostgreSQLContainer("postgres:16.11")
        .withCopyFileToContainer(MountableFile.forHostPath(migrationPath), "/docker-entrypoint-initdb.d/")
        .waitingFor(forListeningPort())
        .apply { start() }

    private var testRepository = with(postgresContainer) { TestRepository(using(jdbcUrl, username, password)) }

    private val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @Test
    fun `should return stored items`() {
        val value = TestClass(
            3,
            "string",
            BigDecimal("0.0000"),
            StringValueObject("date"),
            InstantValueObject(now),
            "{}",
            CustomEnum.ENABLED,
            StringEnum.OTHER,
            DateValueObject(Date.from(now)),
            Date.from(now),
            "custom"
        )

        testRepository.save(value)

        val items = testRepository.findAll()

        assertEquals(value, items[0])
    }

    @AfterEach
    fun tearDown() {
        postgresContainer.stop()
    }
}
