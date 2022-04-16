package io.osguima3.jooqdsl.plugin.converter

import java.time.Instant
import java.time.OffsetDateTime

object InstantForcedType : FromToForcedType(
    fromType = OffsetDateTime::class, toType = Instant::class,
    from = "java.time.OffsetDateTime::toInstant",
    to = "i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC)"
)
