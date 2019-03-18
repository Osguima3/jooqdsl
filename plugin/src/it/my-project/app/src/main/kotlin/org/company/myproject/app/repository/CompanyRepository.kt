package org.company.myproject.app.repository

import org.company.myproject.model.Tables.COMPANY
import org.company.myproject.model.types.Company
import org.jooq.DSLContext
import org.jooq.Record

class CompanyRepository(private val context: DSLContext) {

    fun findAll(): List<Company> =
        context.select(COMPANY.fields().toList())
            .from(COMPANY)
            .fetch(::toCompany)

    private fun toCompany(record: Record): Company = Company(
        record[COMPANY.NAME]!!,
        record[COMPANY.CREATION_DATE]!!,
        record[COMPANY.EMPLOYEES]!!,
        record[COMPANY.LEGAL_TYPE]!!,
        record[COMPANY.INDUSTRY]!!
    )
}
