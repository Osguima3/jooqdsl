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

package io.github.osguima3.jooqdsl.plugin.context

import io.github.osguima3.jooqdsl.model.context.converter
import io.github.osguima3.jooqdsl.plugin.converter.SimpleConverterDefinition
import io.github.osguima3.jooqdsl.plugin.converter.ValueObjectDefinition
import io.github.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.github.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.github.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TableContextImplTest {

    private val jooqContext = mock<JooqContext> {
        on { it.targetPackage } doReturn "package"
    }

    private val tablesContext = TablesContextImpl(jooqContext)

    @Test
    fun `should delegate definition to jooqContext`() {
        with(tablesContext) {
            table("table1") {
                field("field1", KotlinStringValueObject::class)
            }

            table("table2") {
                field("field2") { converter(KotlinConverter::class) }
            }
        }

        val forcedType1 = ValueObjectDefinition(KotlinStringValueObject::class)
        verify(jooqContext).registerForcedType(
            forcedType = forcedType1.toForcedType(".*\\.table1\\.field1")
        )

        val forcedType2 = SimpleConverterDefinition(Int::class, String::class, KotlinConverter::class)
        verify(jooqContext).registerForcedType(
            forcedType = forcedType2.toForcedType(".*\\.table2\\.field2")
        )
    }

    @Test
    fun `should throw IllegalArgumentException if a field is defined twice`() {
        assertThrows<IllegalArgumentException> {
            with(tablesContext) {
                table("table1") {
                    field("field1", KotlinEnum::class)
                    field("field1", KotlinEnum::class)
                }
            }
        }
    }
}
