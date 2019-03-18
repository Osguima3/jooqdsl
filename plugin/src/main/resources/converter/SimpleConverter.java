package org.osguima3.jooqdsl.model.converter;

public class SimpleConverter<T, U> implements org.jooq.Converter<T, U> {

    private Converter<T, U> converter;

    private Class<T> fromType;

    private Class<U> toType;

    public SimpleConverter(Converter<T, U> converter, Class<T> fromType, Class<U> toType) {
        this.converter = converter;
        this.fromType = fromType;
        this.toType = toType;
    }

    @Override
    public U from(T databaseObject) {
        return databaseObject == null ? null : converter.from(databaseObject);
    }

    @Override
    public T to(U userObject) {
        return userObject == null ? null : converter.to(userObject);
    }

    @Override
    public Class<T> fromType() {
        return fromType;
    }

    @Override
    public Class<U> toType() {
        return toType;
    }
}