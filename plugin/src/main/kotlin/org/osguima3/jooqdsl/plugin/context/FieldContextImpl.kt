package org.osguima3.jooqdsl.plugin.context

import org.osguima3.jooqdsl.model.context.FieldContext
import org.osguima3.jooqdsl.model.context.FieldDefinition
import org.osguima3.jooqdsl.model.converter.Converter
import org.osguima3.jooqdsl.plugin.context.TemplateFile.SIMPLE
import org.osguima3.jooqdsl.plugin.context.TemplateFile.TINY_TYPE
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

    override fun <U : Enum<U>> enum(databaseType: String?, userType: KClass<U>): FieldDefinition<U> =
        register(userType, enumString(databaseType, userType))

    override fun <U : Any> tinyType(userType: KClass<U>): FieldDefinition<U> = if (isTinyType(userType)) {
        resolveTinyType(userType)
    } else {
        throw IllegalArgumentException("Type $userType is not a tiny type.")
    }

    override fun <T : Any, U : Any> tinyType(
        converter: KClass<out Converter<T, *>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ): FieldDefinition<U> = if (isTinyType(userType)) {
        registerTinyType(databaseType, userType, constructorString(converter))
    } else {
        throw IllegalArgumentException("Type $userType is not a tiny type.")
    }

    override fun <T : Any, U : Any> custom(
        converter: KClass<out Converter<T, U>>,
        databaseType: KClass<T>,
        userType: KClass<U>
    ): FieldDefinition<U> = registerSimple(databaseType, userType, constructorString(converter))

    internal fun <U : Any> resolve(userType: KClass<U>): FieldDefinition<U> = when {
        isSimple(userType) -> EmptyDefinition() // No need to register
        isInstant(userType) -> register(userType, instantString())
        isEnum(userType) -> register(userType, enumString(null, userType))
        isTinyType(userType) -> resolveTinyType(userType)
        else -> throw IllegalArgumentException("No default mapper available for $userType")
    }

    private fun <U : Any> resolveTinyType(userType: KClass<U>): FieldDefinition<U> {
        val fieldType = userType.singleField.returnType.kotlin
        return when {
            isSimple(fieldType) -> register(userType, tinyTypeString(userType))
            isInstant(fieldType) -> registerTinyType(OffsetDateTime::class, userType, instantString(), setOf(SIMPLE))
            else -> throw IllegalArgumentException("No default mapper available for $userType")
        }
    }

    private fun isSimple(type: KClass<*>) = type.javaPrimitiveType != null || type == String::class

    private fun isInstant(type: KClass<*>) = type == Instant::class

    private fun isEnum(type: KClass<*>) = type.isSubclassOf(Enum::class)

    private fun isTinyType(type: KClass<*>) = type.isData && type.declaredMemberProperties.size == 1

    private fun <T : Any, U : Any> registerSimple(
        databaseType: KClass<T>,
        userType: KClass<U>,
        converter: String,
        templates: Set<TemplateFile> = emptySet()
    ): FieldDefinition<U> = register(
        userType = userType,
        templates = templates + setOf(SIMPLE),
        converter = simpleString(databaseType, userType, converter)
    )

    private fun <T : Any, U : Any> registerTinyType(
        databaseType: KClass<T>,
        userType: KClass<U>,
        converter: String,
        templates: Set<TemplateFile> = emptySet()
    ): FieldDefinition<U> = register(
        userType = userType,
        templates = templates + setOf(TINY_TYPE),
        converter = tinyTypeString(databaseType, userType, converter)
    )

    private fun <U : Any> register(userType: KClass<U>, converter: String, templates: Set<TemplateFile> = emptySet()) =
        EmptyDefinition<U>().also {
            context.addTemplates(templates).registerForcedType(".*\\.$tableName\\.$name", userType, converter)
        }

    private fun constructorString(converter: KClass<*>) =
        "new ${converter.qualifiedName}()"

    private fun instantString() =
        "org.jooq.Converter.ofNullable(java.time.OffsetDateTime.class, java.time.Instant.class, " +
            "java.time.OffsetDateTime::toInstant, i -> java.time.OffsetDateTime.ofInstant(i, java.time.ZoneOffset.UTC))"

    private fun enumString(databaseType: String?, userType: KClass<*>) =
        "new org.jooq.impl.EnumConverter<>(" +
            "${databaseType ?: "${context.targetPackage}.enums.${userType.simpleName}"}.class, " +
            "${userType.simpleName}.class)"

    private fun simpleString(databaseType: KClass<*>, userType: KClass<*>, converter: String) =
        "new ${context.converterPackage}.SimpleConverter<>($converter, " +
            "${databaseType.qualifiedName}.class, ${userType.simpleName}.class)"

    private fun tinyTypeString(userType: KClass<*>, field: Method = userType.singleField) =
        "org.jooq.Converter.ofNullable(${field.returnType.canonicalName}.class, ${userType.simpleName}.class, " +
            "${userType.simpleName}::new, ${userType.simpleName}::${field.name})"

    private fun tinyTypeString(databaseType: KClass<*>, userType: KClass<*>, converter: String) =
        "new ${context.converterPackage}.TinyTypeConverter<>($converter, " +
            "${userType.simpleName}::new, ${userType.simpleName}::${userType.singleField.name}, " +
            "${databaseType.qualifiedName}.class, ${userType.simpleName}.class)"

    private val <U : Any> KClass<U>.singleField
        get() = declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()

    // In the future we could make this more configurable
    inner class EmptyDefinition<U> : FieldDefinition<U>
}
