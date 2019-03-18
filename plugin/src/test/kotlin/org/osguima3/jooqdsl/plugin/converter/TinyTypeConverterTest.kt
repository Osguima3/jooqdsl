package org.osguima3.jooqdsl.plugin.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.osguima3.jooqdsl.model.converter.Converter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TinyTypeConverterTest {

    data class TestInstantTinyType(val value: Instant)

    private val now = Instant.now()
    private val databaseValue = OffsetDateTime.ofInstant(now, ZoneOffset.UTC)
    private val userValue = TestInstantTinyType(now)

    private val baseConverter = mock<Converter<OffsetDateTime, Instant>>().also {
        whenever(it.from(databaseValue)).thenReturn(now)
        whenever(it.to(now)).thenReturn(databaseValue)
    }

    private val converter = loadConverter<OffsetDateTime, TestInstantTinyType>(
        "TinyTypeConverter",
        baseConverter,
        ::TestInstantTinyType.java,
        TestInstantTinyType::value.java,
        OffsetDateTime::class.java,
        TestInstantTinyType::class.java
    )

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
