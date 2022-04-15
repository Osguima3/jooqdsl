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
package io.osguima3.jooqdsl.it.simplejava.repository;

import io.osguima3.jooqdsl.it.simplejava.types.TestClass;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;

import static io.osguima3.jooqdsl.it.simplejava.model.Tables.TEST;

public class TestRepository {

    private final DSLContext context;

    public TestRepository(DSLContext context) {
        this.context = context;
    }

    public List<TestClass> findAll() {
        return context.select(TEST.fields())
            .from(TEST)
            .fetch(this::toObject);
    }

    private TestClass toObject(Record record) {
        return new TestClass(
            record.get(TEST.UUID),
            record.get(TEST.STRING),
            record.get(TEST.INSTANT),
            record.get(TEST.INT),
            record.get(TEST.BIG_DECIMAL),
            record.get(TEST.CUSTOM_ENUM),
            record.get(TEST.STRING_ENUM),
            record.get(TEST.CUSTOM)
        );
    }
}
