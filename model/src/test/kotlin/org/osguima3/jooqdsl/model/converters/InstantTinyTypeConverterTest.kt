package org.osguima3.jooqdsl.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.osguima3.jooqdsl.model.converter.InstantTinyTypeConverter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantTinyTypeConverterTest {

    data class TestInstantTinyType(val value: Instant)

    private val now = Instant.now()
    private val databaseValue = OffsetDateTime.ofInstant(now, ZoneOffset.UTC)
    private val userValue = TestInstantTinyType(now)

    private val converter = InstantTinyTypeConverter(::TestInstantTinyType, TestInstantTinyType::value)

    @Test
    fun testFrom() {
        val result = converter.from(databaseValue)

        assertThat(result).isEqualTo(userValue)
    }

    @Test
    fun testTo() {
        val result = converter.to(userValue)

        assertThat(result).isEqualTo(databaseValue)
    }
}
