package io.osguima3.jooqdsl.model

import io.osguima3.jooqdsl.model.converter.Converter
import org.jooq.Converter as JooqConverter

data class TestValueObject(val value: String)
enum class TestEnum

object TestConverter : Converter<Int, String> {
    override fun from(databaseObject: Int) = ""
    override fun to(userObject: String) = 0
}

object TestJooqConverter : JooqConverter<Int, String> {
    override fun from(databaseObject: Int?) = ""
    override fun to(userObject: String?) = 0
    override fun fromType() = Int::class.java
    override fun toType() = String::class.java
}
