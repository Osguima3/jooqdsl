package org.company.myproject.model.types

data class Company(
    val id: CompanyId,
    val name: CompanyName,
    val creationDate: CompanyCreationDate,
    val employees: CompanyEmployees,
    val legalType: LegalEntityType,
    val industry: CompanyIndustry
)
