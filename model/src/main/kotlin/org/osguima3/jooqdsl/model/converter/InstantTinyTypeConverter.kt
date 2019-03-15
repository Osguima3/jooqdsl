package org.osguima3.jooqdsl.model.converter

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Generic converter for Instant tiny types
 * @Converter OffsetDateTime Instant
 */
class InstantTinyTypeConverter<T>(
    private val fromInstant: (Instant) -> T,
    private val toInstant: (T) -> Instant
) : Converter<OffsetDateTime, T> {

    override fun from(databaseObject: OffsetDateTime) = fromInstant(databaseObject.toInstant())

    override fun to(userObject: T): OffsetDateTime = OffsetDateTime.ofInstant(toInstant(userObject), ZoneOffset.UTC)
}
