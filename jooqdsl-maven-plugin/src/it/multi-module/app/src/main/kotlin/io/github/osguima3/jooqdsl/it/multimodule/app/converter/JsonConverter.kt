package io.github.osguima3.jooqdsl.it.multimodule.app.converter

import io.github.osguima3.jooqdsl.model.converter.SimpleConverter
import org.jooq.JSONB

object JsonConverter : SimpleConverter<JSONB, String>(JSONB::toString, JSONB::valueOf)
