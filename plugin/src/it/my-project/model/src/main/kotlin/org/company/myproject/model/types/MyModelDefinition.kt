package org.company.myproject.model.types

import org.osguima3.jooqdsl.model.ModelDefinition

class MyModelDefinition : ModelDefinition({
    tables {
        table("company") {
            "name" withTinyType CompanyName::class
            "creation_date" withTinyType CompanyCreationDate::class
        }
    }
})
