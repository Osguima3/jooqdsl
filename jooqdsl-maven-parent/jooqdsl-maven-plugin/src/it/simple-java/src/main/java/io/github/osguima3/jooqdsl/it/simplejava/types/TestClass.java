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

package io.github.osguima3.jooqdsl.it.simplejava.types;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class TestClass {

    private final UUID id;

    private final String string;

    private final Instant instant;

    private final Integer integer;

    private final BigDecimal bigDecimal;

    private final CustomEnum customEnum;

    private final StringEnum stringEnum;

    private final Date converter;

    private final String custom;

    public TestClass(
            UUID id, String string, Instant instant, Integer integer, BigDecimal bigDecimal, CustomEnum customEnum,
            StringEnum stringEnum, Date converter, String custom
    ) {
        this.id = id;
        this.string = string;
        this.instant = instant;
        this.integer = integer;
        this.bigDecimal = bigDecimal;
        this.customEnum = customEnum;
        this.stringEnum = stringEnum;
        this.converter = converter;
        this.custom = custom;
    }

    public UUID getId() {
        return id;
    }

    public String getString() {
        return string;
    }

    public Instant getInstant() {
        return instant;
    }

    public Integer getInteger() {
        return integer;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public CustomEnum getCustomEnum() {
        return customEnum;
    }

    public StringEnum getStringEnum() {
        return stringEnum;
    }

    public Date getConverter() {
        return converter;
    }

    public String getCustom() {
        return custom;
    }
}
