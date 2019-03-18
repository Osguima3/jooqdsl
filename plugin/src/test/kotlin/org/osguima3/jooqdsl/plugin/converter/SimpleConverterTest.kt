package org.osguima3.jooqdsl.plugin.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.osguima3.jooqdsl.model.converter.Converter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SimpleConverterTest {

    private val userValue = Instant.now()
    private val databaseValue = OffsetDateTime.ofInstant(userValue, ZoneOffset.UTC)

    private val baseConverter = mock<Converter<OffsetDateTime, Instant>>().also {
        whenever(it.from(databaseValue)).thenReturn(userValue)
        whenever(it.to(userValue)).thenReturn(databaseValue)
    }

    private val converter = loadConverter<OffsetDateTime, Instant>(
        "SimpleConverter",
        baseConverter,
        OffsetDateTime::class.java,
        Instant::class.java
    )

    @Test
    fun testFrom() {
        val result = converter.from(databaseValue)

        assertThat(result).isEqualTo(userValue)

        verify(baseConverter).from(databaseValue)
    }

    @Test
    fun testTo() {
        val result = converter.to(userValue)

        assertThat(result).isEqualTo(databaseValue)

        verify(baseConverter).to(userValue)
    }
}
