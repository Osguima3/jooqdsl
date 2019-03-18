package org.osguima3.jooqdsl.model.converter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Converter from OffsetDateTime to Instant.
 */
public class InstantConverter implements Converter<OffsetDateTime, Instant>, org.jooq.Converter<OffsetDateTime, Instant> {

    private ZoneId zone;

    public InstantConverter() {
        this(ZoneOffset.UTC);
    }

    public InstantConverter(ZoneId zone) {
        this.zone = zone;
    }

    @Override
    public Instant from(OffsetDateTime databaseObject) {
        return databaseObject == null ? null : databaseObject.toInstant();
    }

    @Override
    public OffsetDateTime to(Instant userObject) {
        return userObject == null ? null : OffsetDateTime.ofInstant(userObject, zone);
    }

    @Override
    public Class<OffsetDateTime> fromType() {
        return OffsetDateTime.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
