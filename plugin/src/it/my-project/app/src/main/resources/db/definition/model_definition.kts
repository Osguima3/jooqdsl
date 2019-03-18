import org.company.myproject.model.types.CompanyCreationDate
import org.company.myproject.model.types.CompanyEmployees
import org.company.myproject.model.types.CompanyId
import org.company.myproject.model.types.CompanyIndustry
import org.company.myproject.model.types.CompanyName
import org.company.myproject.model.types.LegalEntityType
import org.osguima3.jooqdsl.model.ModelDefinition

ModelDefinition {
    tables {
        table("company") {
            field("id", CompanyId::class)
            field("name", CompanyName::class)
            field("creation_date", CompanyCreationDate::class)
            field("employees", CompanyEmployees::class)
            field("legal_type", LegalEntityType::class)
            field("industry") { enum("String", CompanyIndustry::class) }
        }
    }
}
