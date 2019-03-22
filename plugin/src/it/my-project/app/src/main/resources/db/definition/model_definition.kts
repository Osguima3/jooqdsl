/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 */

import org.company.myproject.model.types.CompanyCreationDate
import org.company.myproject.model.types.CompanyEmployees
import org.company.myproject.model.types.CompanyId
import org.company.myproject.model.types.CompanyIndustry
import org.company.myproject.model.types.CompanyName
import org.company.myproject.model.types.CompanyValuation
import org.company.myproject.model.types.LegalEntityType
import org.osguima3.jooqdsl.model.ModelDefinition

ModelDefinition {
    tables {
        table("company") {
            field("id", CompanyId::class)
            field("name", CompanyName::class)
            field("creation_date", CompanyCreationDate::class)
            field("employees", CompanyEmployees::class)
            field("valuation", CompanyValuation::class)
            field("legal_type", LegalEntityType::class)
            field("industry") { enum(CompanyIndustry::class, "String") }
        }
    }
}
