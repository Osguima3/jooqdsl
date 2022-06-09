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

package io.github.osguima3.jooqdsl.it.simplejava.converter;

import io.github.osguima3.jooqdsl.model.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateConverter implements Converter<String, Date> {

    private final SimpleDateFormat formatter = new SimpleDateFormat();

    @SuppressWarnings("unused")
    public static final SimpleDateConverter INSTANCE = new SimpleDateConverter();

    @Override
    public Date from(String databaseObject) {
        try {
            return formatter.parse(databaseObject);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse date " + databaseObject);
        }
    }

    @Override
    public String to(Date userObject) {
        return formatter.format(userObject);
    }
}
