/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 */

package io.osguima3.jooqdsl.plugin.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.osguima3.jooqdsl.model.converter.Converter
import io.osguima3.jooqdsl.plugin.InstantValueObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Disabled
class ValueObjectConverterTest {

    private val now = Instant.now()
    private val databaseValue = OffsetDateTime.ofInstant(now, ZoneOffset.UTC)
    private val userValue = InstantValueObject(now)

    private val baseConverter = mock<Converter<OffsetDateTime, Instant>>().also {
        whenever(it.from(databaseValue)).thenReturn(now)
        whenever(it.to(now)).thenReturn(databaseValue)
    }

    private val converter = loadConverter<OffsetDateTime, InstantValueObject>(
        TemplateFile.VALUE_OBJECT,
        loadAdapterConverter(baseConverter),
        ::InstantValueObject.java,
        InstantValueObject::value.java,
        OffsetDateTime::class.java,
        InstantValueObject::class.java
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
