package io.github.osguima3.jooqdsl.core.types;

import io.github.osguima3.jooqdsl.model.converter.Converter;

public class JavaConverter implements Converter<Integer, String> {

    public static final JavaConverter INSTANCE = new JavaConverter();

    @Override
    public String from(Integer databaseObject) {
        return "";
    }

    @Override
    public Integer to(String userObject) {
        return 0;
    }
}
