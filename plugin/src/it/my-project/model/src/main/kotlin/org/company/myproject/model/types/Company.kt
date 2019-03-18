package org.company.myproject.model.types

data class Company(
    val name: CompanyName,
    val creationDate: CompanyCreationDate,
    val employees: CompanyEmployees,
    val legalType: LegalEntityType,
    val industry: CompanyIndustry
)
