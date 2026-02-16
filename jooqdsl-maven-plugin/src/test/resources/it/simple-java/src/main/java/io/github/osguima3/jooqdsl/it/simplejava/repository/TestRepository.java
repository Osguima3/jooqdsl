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

import io.github.osguima3.jooqdsl.it.simplejava.model.tables.records.TestRecord;
import io.github.osguima3.jooqdsl.it.simplejava.types.TestClass;
import org.jooq.DSLContext;

import java.util.List;

import static io.github.osguima3.jooqdsl.it.simplejava.model.Tables.TEST;

public class TestRepository {

    private final DSLContext context;

    public TestRepository(DSLContext context) {
        this.context = context;
    }

    public List<TestClass> findAll() {
        return context.selectFrom(TEST)
            .fetchInto(TEST)
            .map(this::toObject);
    }

    public void save(TestClass test) {
        context.insertInto(TEST)
            .set(TEST.INT, test.integer())
            .set(TEST.STRING, test.string())
            .set(TEST.BIG_DECIMAL, test.bigDecimal())
            .set(TEST.VALUE_OBJECT, test.valueObject())
            .set(TEST.INSTANT_OBJECT, test.instantObject())
            .set(TEST.JSON, test.json())
            .set(TEST.CUSTOM_ENUM, test.customEnum())
            .set(TEST.STRING_ENUM, test.stringEnum())
            .set(TEST.COMPOSITE, test.composite())
            .set(TEST.CONVERTER, test.converter())
            .set(TEST.CUSTOM, test.custom())
            .execute();
    }

    private TestClass toObject(TestRecord record) {
        return new TestClass(
            record.getInt(),
            record.getString(),
            record.getBigDecimal(),
            record.getValueObject(),
            record.getInstantObject(),
            record.getJson(),
            record.getCustomEnum(),
            record.getStringEnum(),
            record.getComposite(),
            record.getConverter(),
            record.getCustom()
        );
    }
}
