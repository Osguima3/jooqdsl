package org.osguima3.jooqdsl.model.definition

import kotlin.reflect.KClass

interface ConverterContext<T : Any, U : Any> {

    infix fun from(fromType: KClass<T>): ConverterContext<T, U>

    infix fun to(toType: KClass<U>): ConverterContext<T, U>
}
