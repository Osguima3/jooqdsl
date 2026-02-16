package io.github.osguima3.jooqdsl.core.types;

public class JavaValueObjectWithMethods {

    private final String value;

    public JavaValueObjectWithMethods(String value) {
        this.value = value;
    }

    public String getOther() {
        return "other";
    }

    public String getValue() {
        return value;
    }
}
