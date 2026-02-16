package io.github.osguima3.jooqdsl.core.types;

public class JavaValueObject {

    private final String value;

    public JavaValueObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
