package io.osguima3.it.jooqdsl.multimodule.app.repository

import io.osguima3.it.jooqdsl.multimodule.model.types.BigDecimalValueObject
import io.osguima3.it.jooqdsl.multimodule.model.types.CustomEnum
import io.osguima3.it.jooqdsl.multimodule.model.types.DateValueObject
import io.osguima3.it.jooqdsl.multimodule.model.types.IdValueObject
import io.osguima3.it.jooqdsl.multimodule.model.types.InstantValueObject
import io.osguima3.it.jooqdsl.multimodule.model.types.IntValueObject
import io.osguima3.it.jooqdsl.multimodule.model.types.StringEnum
import io.osguima3.it.jooqdsl.multimodule.model.types.StringValueObject
import io.osguima3.it.jooqdsl.multimodule.model.types.TestClass
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

