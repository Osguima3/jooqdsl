import org.company.myproject.model.types.CompanyCreationDate
import org.company.myproject.model.types.CompanyName
import org.osguima3.jooqdsl.model.ModelDefinition

ModelDefinition {
    tables {
        table("company") {
            "name" withTinyType CompanyName::class
            "creation_date" withTinyType CompanyCreationDate::class
        }
    }
}
