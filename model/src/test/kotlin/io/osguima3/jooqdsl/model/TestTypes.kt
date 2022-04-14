package io.osguima3.jooqdsl.model

import io.osguima3.jooqdsl.model.converter.Converter

data class TestValueObject(val value: String)
enum class TestEnum

object TestConverter : Converter<Int, String> {
    override fun from(databaseObject: Int) = ""
    override fun to(userObject: String) = 0
}
