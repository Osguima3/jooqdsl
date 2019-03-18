package org.osguima3.jooqdsl.model.converter;

import java.util.function.Function;

/**
 * Generic converter for tiny types.
 */
public class TinyTypeConverter<T, U, V> implements org.jooq.Converter<T, V> {

    private Converter<T, U> converter;

    private Function<U, V> fromTinyType;

    private Function<V, U> toTinyType;

    private Class<T> fromType;

    private Class<V> toType;

    public TinyTypeConverter(
            Converter<T, U> converter,
            Function<U, V> fromTinyType,
            Function<V, U> toTinyType,
            Class<T> fromType,
            Class<V> toType
    ) {
        this.converter = converter;
        this.fromTinyType = fromTinyType;
        this.toTinyType = toTinyType;
        this.fromType = fromType;
        this.toType = toType;
    }

    @Override
    public V from(T databaseObject) {
        return databaseObject == null ? null : fromTinyType.apply(converter.from(databaseObject));
    }

    @Override
    public T to(V userObject) {
        return userObject == null ? null : converter.to(toTinyType.apply(userObject));
    }

    @Override
    public Class<T> fromType() {
        return fromType;
    }

    @Override
    public Class<V> toType() {
        return toType;
    }
}
