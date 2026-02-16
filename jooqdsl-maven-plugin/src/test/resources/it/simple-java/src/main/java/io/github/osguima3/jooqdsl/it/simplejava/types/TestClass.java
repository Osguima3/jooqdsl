package io.github.osguima3.jooqdsl.it.simplejava.types;

import java.math.BigDecimal;
import java.util.Date;

public record TestClass(
    Integer integer,
    String string,
    BigDecimal bigDecimal,
    StringValueObject valueObject,
    InstantValueObject instantObject,
    String json,
    CustomEnum customEnum,
    StringEnum stringEnum,
    DateValueObject composite,
    Date converter,
    String custom
) {
}
