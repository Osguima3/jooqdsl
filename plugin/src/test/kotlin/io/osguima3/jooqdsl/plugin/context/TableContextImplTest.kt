package io.osguima3.jooqdsl.plugin.context

import io.osguima3.jooqdsl.model.context.converter
import io.osguima3.jooqdsl.plugin.converter.ConverterForcedType
import io.osguima3.jooqdsl.plugin.converter.ValueObjectForcedType
import io.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.osguima3.jooqdsl.plugin.types.KotlinEnum
import io.osguima3.jooqdsl.plugin.types.KotlinStringValueObject
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

        verify(jooqContext).registerForcedType(
            expression = ".*\\.table1\\.field1",
            forcedType = ValueObjectForcedType(KotlinStringValueObject::class)
        )
        verify(jooqContext).registerForcedType(
            expression = ".*\\.table2\\.field2",
            forcedType = ConverterForcedType(Int::class, String::class, KotlinConverter::class)
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
