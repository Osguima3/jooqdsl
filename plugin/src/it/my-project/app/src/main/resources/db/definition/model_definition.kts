import org.company.myproject.model.types.*
import org.osguima3.jooqdsl.model.ModelDefinition

ModelDefinition {
    tables {
        table("company") {
            field("name", CompanyName::class)
            field("creation_date", CompanyCreationDate::class)
            field("employees", CompanyEmployees::class)
            field("legal_type", LegalEntityType::class)
            field("industry") { enum("String", CompanyIndustry::class) }
        }
    }
}
