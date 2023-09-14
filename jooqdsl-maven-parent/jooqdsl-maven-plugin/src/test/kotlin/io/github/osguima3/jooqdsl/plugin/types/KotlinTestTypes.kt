package io.github.osguima3.jooqdsl.plugin.types

import io.github.osguima3.jooqdsl.model.converter.Converter
import io.github.osguima3.jooqdsl.model.converter.SimpleConverter
import java.time.Instant
import org.jooq.Converter as JooqConverter

enum class KotlinEnum
data class KotlinStringValueObject(val value: String)
data class KotlinInstantValueObject(val value: Instant)
data class KotlinUnsupportedValueObject(val value: KotlinEnum)
data class KotlinUnsupportedObject(val field1: String, val field2: String)

object KotlinConverter : SimpleConverter<Int, String>(Int::toString, String::toInt)

object KotlinJooqConverter : JooqConverter<Int, String> {
    override fun from(databaseObject: Int): String = ""
    override fun to(userObject: String): Int = 0
    override fun fromType() = Int::class.java
    override fun toType() = String::class.java
}

object KotlinInvalidConverter : Converter<Int, String> {
    override fun from(databaseObject: Int): String = ""
    override fun to(userObject: String): Int = 0
}
