package io.osguima3.jooqdsl.model

import io.osguima3.jooqdsl.model.converter.SimpleConverter

data class TestValueObject(val value: String)
enum class TestEnum

object TestConverter : SimpleConverter<Int, String>(Int::toString, String::toInt)

object TestJooqConverter : JooqConverter<Int, String> {
    override fun from(databaseObject: Int) = ""
    override fun to(userObject: String) = 0
    override fun fromType() = Int::class.java
    override fun toType() = String::class.java
}
