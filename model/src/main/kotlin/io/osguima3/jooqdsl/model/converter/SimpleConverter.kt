package io.osguima3.jooqdsl.model.converter

open class SimpleConverter<T, U>(private val from: (T) -> U, private val to: (U) -> T) : Converter<T, U> {
    override fun from(databaseObject: T) = from.invoke(databaseObject)
    override fun to(userObject: U) = to.invoke(userObject)
}
