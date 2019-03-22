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
        record[COMPANY.ID]!!,
        record[COMPANY.NAME]!!,
        record[COMPANY.CREATION_DATE]!!,
        record[COMPANY.EMPLOYEES]!!,
        record[COMPANY.LEGAL_TYPE]!!,
        record[COMPANY.INDUSTRY]!!
    )
}
