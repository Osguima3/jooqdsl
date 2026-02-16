/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
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

import io.github.osguima3.jooqdsl.it.simplejava.converter.JsonConverter
import io.github.osguima3.jooqdsl.it.simplejava.converter.SimpleDateConverter
import io.github.osguima3.jooqdsl.it.simplejava.types.CustomEnum
import io.github.osguima3.jooqdsl.it.simplejava.types.DateValueObject
import io.github.osguima3.jooqdsl.it.simplejava.types.InstantValueObject
import io.github.osguima3.jooqdsl.it.simplejava.types.StringEnum
import io.github.osguima3.jooqdsl.it.simplejava.types.StringValueObject
import io.github.osguima3.jooqdsl.model.ModelDefinition
import io.github.osguima3.jooqdsl.model.context.converter
import io.github.osguima3.jooqdsl.model.context.valueObject
import java.math.BigDecimal

ModelDefinition {
    tables {
        table("test") {
            field("int", Int::class)
            field("string", String::class)
            field("big_decimal", BigDecimal::class)
            field("value_object", StringValueObject::class)
            field("instant_object", InstantValueObject::class)
            field("json") { converter(JsonConverter::class) }
            field("custom_enum", CustomEnum::class)
            field("string_enum") { enum(StringEnum::class, "String") }
            field("composite") { valueObject(SimpleDateConverter::class, DateValueObject::class) }
            field("converter") { converter(SimpleDateConverter::class) }
            field("custom") { custom(String::class, "org.jooq.Converters.identity(String.class)") }
        }
    }
}
