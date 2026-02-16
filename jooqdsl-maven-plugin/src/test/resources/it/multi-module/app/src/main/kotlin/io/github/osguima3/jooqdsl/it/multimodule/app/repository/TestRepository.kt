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

import io.github.osguima3.jooqdsl.it.multimodule.app.model.tables.records.TestRecord
import io.github.osguima3.jooqdsl.it.multimodule.app.model.tables.references.TEST
import io.github.osguima3.jooqdsl.it.multimodule.model.types.TestClass
import org.jooq.DSLContext

class TestRepository(private val context: DSLContext) {

    fun findAll(): List<TestClass> =
        context.selectFrom(TEST)
            .fetchInto(TEST)
            .map(::toObject)

    fun save(test: TestClass) {
        context.insertInto(TEST)
            .set(TEST.INT, test.int)
            .set(TEST.STRING, test.string)
            .set(TEST.BIG_DECIMAL, test.bigDecimal)
            .set(TEST.VALUE_OBJECT, test.valueObject)
            .set(TEST.INSTANT_OBJECT, test.instantObject)
            .set(TEST.JSON, test.json)
            .set(TEST.CUSTOM_ENUM, test.customEnum)
            .set(TEST.STRING_ENUM, test.stringEnum)
            .set(TEST.COMPOSITE, test.composite)
            .set(TEST.CONVERTER, test.converter)
            .set(TEST.CUSTOM, test.custom)
            .execute()
    }

    private fun toObject(record: TestRecord) = TestClass(
        record.int!!,
        record.string!!,
        record.bigDecimal!!,
        record.valueObject!!,
        record.instantObject!!,
        record.json!!,
        record.customEnum!!,
        record.stringEnum!!,
        record.composite!!,
        record.converter!!,
        record.custom!!
    )
}
