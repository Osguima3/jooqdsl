package io.osguima3.jooqdsl.multimodule.app.converter

import io.osguima3.jooqdsl.model.converter.Converter
import java.text.SimpleDateFormat
import java.util.Date

class SimpleDateConverter : Converter<String, Date> {

    private val formatter = SimpleDateFormat()

    override fun from(databaseObject: String): Date = formatter.parse(databaseObject)

    override fun to(userObject: Date): String = formatter.format(userObject)
}
