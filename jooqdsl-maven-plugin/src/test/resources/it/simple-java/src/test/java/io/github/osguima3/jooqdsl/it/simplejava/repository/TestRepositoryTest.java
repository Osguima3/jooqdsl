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

package io.github.osguima3.jooqdsl.it.simplejava.repository;

import io.github.osguima3.jooqdsl.it.simplejava.types.*;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRepositoryTest {

    private final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    private final String migrationPath = this.getClass().getResource("/db/migration").getPath();

    private final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:16.11")
        .withCopyFileToContainer(MountableFile.forHostPath(migrationPath), "/docker-entrypoint-initdb.d/")
        .waitingFor(Wait.forListeningPort());

    {
        postgresContainer.start();
    }

    private final TestRepository testRepository = new TestRepository(
        DSL.using(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())
    );

    @Test
    public void shouldReturnStoredItems() {
        TestClass value = new TestClass(
            3,
            "string",
            new BigDecimal("0.0000"),
            new StringValueObject("date"),
            new InstantValueObject(now),
            "{}",
            CustomEnum.ENABLED,
            StringEnum.OTHER,
            new DateValueObject(Date.from(now)),
            Date.from(now),
            "custom"
        );

        testRepository.save(value);

        List<TestClass> items = testRepository.findAll();

        assertEquals(value, items.get(0));
    }

    @AfterEach
    public void tearDown() {
        postgresContainer.stop();
    }
}

