package io.osguima3.jooqdsl.plugin

import io.osguima3.jooqdsl.plugin.converter.INSTANCE_FIELD
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

internal val KClass<*>.isPrimitive
    get() = when (this) {
        BigDecimal::class, String::class, UUID::class -> true
        else -> javaPrimitiveType != null
    }

internal val KClass<*>.isEnum
    get() = isSubclassOf(Enum::class)

internal val KClass<*>.isValueObject
    get() = isData && declaredMemberProperties.size == 1

internal val KClass<*>.isObjectType
    get() = objectInstance != null || java.declaredFields.any {
        it.name == INSTANCE_FIELD && it.type == java && Modifier.isStatic(it.modifiers)
    }

internal val KClass<*>.simple: String
    get() = javaObjectType.simpleName

internal val KClass<*>.qualified: String
    get() = javaObjectType.canonicalName

internal val KClass<*>.valueField: String
    get() = valueGetter.name

internal val KClass<*>.valueType: KClass<out Any>
    get() = valueGetter.returnType.kotlin

internal val KClass<*>.valueGetter: Method
    get() = if (!isValueObject) {
        throw IllegalArgumentException("Type $this is not a value object.")
    } else {
        declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()
    }
