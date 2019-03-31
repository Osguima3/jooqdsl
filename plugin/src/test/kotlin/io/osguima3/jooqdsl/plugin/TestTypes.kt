package io.osguima3.jooqdsl.plugin

import io.osguima3.jooqdsl.model.converter.Converter
import java.time.Instant

data class IntValueObject(val value: Int)
data class StringValueObject(val value: String)
data class InstantValueObject(val value: Instant)
enum class TestEnum

abstract class IntStringConverter : Converter<Int, String>
