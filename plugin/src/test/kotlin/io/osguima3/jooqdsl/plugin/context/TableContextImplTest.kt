package io.osguima3.jooqdsl.plugin.context

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.plugin.TestEnum
import io.osguima3.jooqdsl.plugin.TestKotlinConverter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass

class TableContextImplTest {

    private val jooqContext = mock<JooqContext>().also {
        whenever(it.targetPackage).thenReturn("package")
    }

    private val tablesContext = TablesContextImpl(jooqContext)

    @Test
    fun `should delegate definition to jooqContext`() {
        tablesContext.run {
            table("table1") {
                field("field1", TestEnum::class)
            }

            table("table2") {
                field("field2") { custom(TestKotlinConverter::class) }
            }
        }

        verify(jooqContext).registerForcedType(
            expression = ".*\\.table1\\.field1",
            userType = TestEnum::class,
            converter = "new org.jooq.impl.EnumConverter<>(package.enums.TestEnum.class, TestEnum.class)"
        )
        verify(jooqContext).registerForcedType(
            expression = ".*\\.table2\\.field2",
            userType = String::class,
            converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                "${TestKotlinConverter::class.qualified}.INSTANCE::from, " +
                "${TestKotlinConverter::class.qualified}.INSTANCE::to)"
        )
    }

    @Test
    fun `should throw IllegalArgumentException if a field is defined twice`() {
        assertThrows<IllegalArgumentException> {
            tablesContext.run {
                table("table1") {
                    field("field1", TestEnum::class)
                    field("field1", TestEnum::class)
                }
            }
        }
    }

    private val KClass<*>.qualified get() = javaObjectType.canonicalName
}
