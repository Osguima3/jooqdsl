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

import io.github.osguima3.jooqdsl.it.multimodule.model.types.BigDecimalValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.CustomEnum
import io.github.osguima3.jooqdsl.it.multimodule.model.types.DateValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.IdValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.InstantValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.IntValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.StringEnum
import io.github.osguima3.jooqdsl.it.multimodule.model.types.StringValueObject
import io.github.osguima3.jooqdsl.it.multimodule.model.types.TestClass
import junit.framework.TestCase.assertEquals
import org.jooq.impl.DSL.using
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.time.Instant
import java.util.Date
import java.util.UUID

class TestRepositoryTest {

    private val migrationPath = this::class.java.getResource("/db/migration")!!.path

    private var postgresContainer = PostgresContainer()
        .withFileSystemBind(migrationPath, "/docker-entrypoint-initdb.d/")
        .apply { start() }

    private var testRepository = with(postgresContainer) { TestRepository(using(jdbcUrl, username, password)) }

    @Test
    fun `should return stored items`() {
        val value = TestClass(
            IdValueObject(UUID.randomUUID()),
            StringValueObject("value"),
            InstantValueObject(Instant.now()),
            IntValueObject(0),
            BigDecimalValueObject(BigDecimal.ZERO),
            "{}",
            CustomEnum.ENABLED,
            StringEnum.OTHER,
            DateValueObject(Date.from(Instant.now())),
            Date.from(Instant.now()),
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

class PostgresContainer : PostgreSQLContainer<PostgresContainer>("postgres:10.6")

