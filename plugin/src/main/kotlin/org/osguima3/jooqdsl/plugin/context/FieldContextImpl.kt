package org.osguima3.jooqdsl.plugin.context

import org.osguima3.jooqdsl.model.context.FieldContext
import org.osguima3.jooqdsl.model.context.FieldDefinition
import org.osguima3.jooqdsl.model.converter.Converter
import java.lang.reflect.Method
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

class FieldContextImpl(
    private val context: ModelContextImpl,
    private val tableName: String,
    private val name: String
) : FieldContext {

    override fun <T : Enum<T>> enum(fromType: String?, toType: KClass<T>): FieldDefinition<T> =
        registerEnum(fromType, toType)

    override fun <T : Any> tinyType(toType: KClass<T>): FieldDefinition<T> = if (isTinyType(toType))
        registerTinyType(toType)
    else
        throw IllegalArgumentException("Type $toType is not a tiny type.")

    override fun <T : Any, U, V : Any> tinyType(
        converter: KClass<out Converter<T, U>>,
        fromType: KClass<T>,
        toType: KClass<V>
    ): FieldDefinition<V> = if (isTinyType(toType))
        registerCustomTinyType(fromType, toType, toType.singleField, converter.qualifiedName!!)
    else
        throw IllegalArgumentException("Type $toType is not a tiny type.")

    override fun <T : Any, U : Any> custom(
        converter: KClass<out Converter<T, U>>,
        fromType: KClass<T>,
        toType: KClass<U>
    ) = registerConverter(fromType, toType, converter.qualifiedName!!)

//    override fun <T : Any, U : Any> custom(
//        from: (T) -> U,
//        to: (U) -> T,
//        fromType: KClass<T>,
//        toType: KClass<U>
//    ): FieldDefinition<U> = TODO("Not implemented yet!")

    @Suppress("UNCHECKED_CAST")
    internal fun <U : Any> resolve(userType: KClass<U>): FieldDefinition<U> = when {
        isSimple(userType) -> EmptyDefinition() // No need to register
        isInstant(userType) -> registerInstant() as FieldDefinition<U>
        isEnum(userType) -> registerEnum(null, userType)
        isTinyType(userType) -> registerTinyType(userType)
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun isSimple(type: KClass<*>) = type.javaPrimitiveType != null || type == String::class

    private fun isInstant(type: KClass<*>) = type == Instant::class

    private fun isEnum(type: KClass<*>) = type.isSubclassOf(Enum::class)

    private fun isTinyType(type: KClass<*>) = type.isData && type.declaredMemberProperties.size == 1

    private fun registerInstant(): FieldDefinition<Instant> {
        context.generateConverter("InstantConverter")
        return registerForcedType(
            userType = Instant::class,
            converter = "${context.converterPackage}.InstantConverter"
        )
    }

    private fun <U : Any> registerEnum(databaseType: String?, userType: KClass<U>): FieldDefinition<U> =
        registerForcedType(
            userType = userType,
            converter = "new org.jooq.impl.EnumConverter<>(" +
                    "${databaseType ?: "${context.targetPackage}.enums.${userType.simpleName}"}.class, " +
                    "${userType.simpleName}.class)"
        )

    private fun <U : Any> registerTinyType(userType: KClass<U>): FieldDefinition<U> {
        val field = userType.singleField
        return when {
            isSimple(field.returnType.kotlin) -> registerSimpleTinyType(userType, field)
            isInstant(field.returnType.kotlin) -> registerInstantTinyType(userType, field)
            else -> throw IllegalArgumentException("No default mapper available for $userType")
        }
    }

    private fun <U : Any> registerSimpleTinyType(userType: KClass<U>, field: Method): FieldDefinition<U> =
        registerForcedType(
            userType = userType,
            converter = "org.jooq.Converter.ofNullable(${field.returnType.canonicalName}.class, " +
                    "${userType.simpleName}.class, ${userType.simpleName}::new, ${userType.simpleName}::${field.name})"
        )

    private fun <U : Any> registerInstantTinyType(userType: KClass<U>, field: Method): FieldDefinition<U> {
        context.generateConverter("InstantConverter")
        return registerCustomTinyType(
            databaseType = OffsetDateTime::class,
            userType = userType,
            field = field,
            converter = "${context.converterPackage}.InstantConverter"
        )
    }

    private fun <D : Any, U : Any> registerCustomTinyType(
        databaseType: KClass<D>,
        userType: KClass<U>,
        field: Method,
        converter: String
    ): FieldDefinition<U> {
        context.generateConverter("TinyTypeConverter")
        return registerForcedType(
            userType = userType,
            converter = "new ${context.converterPackage}.TinyTypeConverter<>(new $converter(), " +
                    "${userType.simpleName}::new, ${userType.simpleName}::${field.name}, " +
                    "${databaseType.qualifiedName}.class, ${userType.simpleName}.class)"
        )
    }

    private fun <D : Any, U : Any> registerConverter(
        databaseType: KClass<D>,
        userType: KClass<U>,
        converter: String
    ): FieldDefinition<U> {
        context.generateConverter("SimpleConverter")
        return registerForcedType(
            userType = userType,
            converter = "new ${context.converterPackage}.SimpleConverter<>($converter, " +
                    "${databaseType.qualifiedName}.class, ${userType.simpleName}.class)"
        )
    }

    private fun <U> registerForcedType(userType: KClass<*>, converter: String): FieldDefinition<U> = context
        .registerForcedType(".*\\.$tableName\\.$name", userType, converter)
        .let { return EmptyDefinition() }

    private val <U : Any> KClass<U>.singleField
        get() = declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()

    // In the future we could make this more configurable
    inner class EmptyDefinition<T> : FieldDefinition<T>
}
