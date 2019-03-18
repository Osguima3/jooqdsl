package org.osguima3.jooqdsl.plugin.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantConverterTest {

    private val userValue = Instant.now()
    private val databaseValue = OffsetDateTime.ofInstant(userValue, ZoneOffset.UTC)

    private val converter = loadConverter<OffsetDateTime, Instant>(
        "InstantConverter"
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
