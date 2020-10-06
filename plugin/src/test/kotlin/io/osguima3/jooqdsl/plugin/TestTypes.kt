package io.osguima3.jooqdsl.plugin

import io.osguima3.jooqdsl.model.converter.Converter
import java.time.Instant

data class TestValueObject(val value: String)
data class TestInstantValueObject(val value: Instant)
data class TestUnsupportedObject(val field1: String, val field2: Int)
data class TestUnsupportedValueObject(val value: TestEnum)
enum class TestEnum

object TestKotlinConverter : Converter<Int, String> {
    override fun from(databaseObject: Int) = ""
    override fun to(userObject: String) = 0
}

class TestJavaConverter : Converter<Int, String> {
    override fun from(databaseObject: Int) = ""
    override fun to(userObject: String) = 0

    companion object {

        @JvmStatic
        @Suppress("unused")
        val INSTANCE = TestJavaConverter()
    }
}

class TestInvalidConverter : Converter<Int, String> {
    override fun from(databaseObject: Int) = ""
    override fun to(userObject: String) = 0
}
