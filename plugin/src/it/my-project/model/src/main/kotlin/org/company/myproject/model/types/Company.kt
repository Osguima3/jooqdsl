package org.company.myproject.model.types

import java.time.OffsetDateTime

data class Company(
    val name: CompanyName,
    val creationDate: OffsetDateTime
)
