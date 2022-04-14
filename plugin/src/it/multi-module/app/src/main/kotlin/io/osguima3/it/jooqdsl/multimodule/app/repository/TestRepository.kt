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

package io.osguima3.it.jooqdsl.multimodule.app.repository

import io.osguima3.it.jooqdsl.multimodule.app.model.Tables.TEST
import io.osguima3.it.jooqdsl.multimodule.model.types.TestClass
import org.jooq.DSLContext
import org.jooq.Record

class TestRepository(private val context: DSLContext) {

    fun findAll(): List<TestClass> =
        context.select(TEST.fields().toList())
            .from(TEST)
            .fetch(::toObject)

    private fun toObject(record: Record): TestClass = TestClass(
        record[TEST.UUID]!!,
        record[TEST.STRING]!!,
        record[TEST.INSTANT]!!,
        record[TEST.INT]!!,
        record[TEST.BIG_DECIMAL]!!,
        record[TEST.CUSTOM_ENUM]!!,
        record[TEST.STRING_ENUM]!!,
        record[TEST.CUSTOM_VALUE_OBJECT]
    )
}
