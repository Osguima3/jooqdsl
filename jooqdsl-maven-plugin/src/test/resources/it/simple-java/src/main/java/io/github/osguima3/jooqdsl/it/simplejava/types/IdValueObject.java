package io.github.osguima3.jooqdsl.it.simplejava.types;

import java.util.UUID;

public class IdValueObject {

    private final UUID value;

    public IdValueObject(UUID value) {
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IdValueObject that = (IdValueObject) obj;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "IdValueObject{value=" + value + '}';
    }
}
