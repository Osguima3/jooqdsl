package io.osguima3.jooqdsl.plugin.context

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import io.osguima3.jooqdsl.model.context.custom
import io.osguima3.jooqdsl.plugin.types.KotlinConverter
import io.osguima3.jooqdsl.plugin.types.KotlinEnum
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
                field("field1", KotlinEnum::class)
            }

            table("table2") {
                field("field2") { custom(KotlinConverter::class) }
            }
        }

        verify(jooqContext).registerForcedType(
            expression = ".*\\.table1\\.field1",
            userType = KotlinEnum::class,
            converter = "new org.jooq.impl.EnumConverter<>(package.enums.KotlinEnum.class, KotlinEnum.class)"
        )
        verify(jooqContext).registerForcedType(
            expression = ".*\\.table2\\.field2",
            userType = String::class,
            converter = "org.jooq.Converter.ofNullable(java.lang.Integer.class, String.class, " +
                "${KotlinConverter::class.qualified}.INSTANCE::from, " +
                "${KotlinConverter::class.qualified}.INSTANCE::to)"
        )
    }

    @Test
    fun `should throw IllegalArgumentException if a field is defined twice`() {
        assertThrows<IllegalArgumentException> {
            tablesContext.run {
                table("table1") {
                    field("field1", KotlinEnum::class)
                    field("field1", KotlinEnum::class)
                }
            }
        }
    }

    private val KClass<*>.qualified get() = javaObjectType.canonicalName
}
