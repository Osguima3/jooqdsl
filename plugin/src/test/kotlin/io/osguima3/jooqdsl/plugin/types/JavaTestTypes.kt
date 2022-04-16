package io.osguima3.jooqdsl.plugin.types

import io.osguima3.jooqdsl.model.converter.Converter

class JavaConverter : Converter<Int, String> {

    override fun from(databaseObject: Int) = ""

    override fun to(userObject: String) = 0

    companion object {

        @Suppress("unused")
        val INSTANCE = JavaConverter()
    }
}

class JavaInvalidConverter : Converter<Int, String> {

    override fun from(databaseObject: Int): String = ""

    override fun to(userObject: String): Int = 0
}

class JavaValueObject(value: String) {

    private val field = value

    fun getField() = field
}

class JavaUnsupportedObject(value1: String, value2: Int) {

    private val field1 = value1

    private val field2 = value2

    fun getField1() = field1

    fun getField2() = field2
}
