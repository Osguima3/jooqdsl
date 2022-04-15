package io.osguima3.jooqdsl.plugin.types

import io.osguima3.jooqdsl.model.converter.Converter
import java.time.Instant

enum class KotlinEnum
data class KotlinStringValueObject(val value: String)
data class KotlinInstantValueObject(val value: Instant)
data class KotlinUnsupportedValueObject(val value: KotlinEnum)
data class KotlinUnsupportedObject(val field1: String, val field2: String)

object KotlinConverter : Converter<Int, String> {

    override fun from(databaseObject: Int) = ""

    override fun to(userObject: String) = 0
}

class KotlinInvalidConverter : Converter<Int, String> {

    override fun from(databaseObject: Int) = ""

    override fun to(userObject: String) = 0
}
