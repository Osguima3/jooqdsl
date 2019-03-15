package org.osguima3.jooqdsl.plugin.visitor

import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.osguima3.jooqdsl.model.converter.Converter
import org.osguima3.jooqdsl.model.converter.InstantTinyTypeConverter
import org.osguima3.jooqdsl.model.definition.ConverterContext
import org.osguima3.jooqdsl.model.definition.TableContext
import java.lang.reflect.Method
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter

typealias ForcedTypes = MutableList<ForcedType>

class TableContextImpl(private val configuration: Configuration, private val name: String) : TableContext {

    private val forcedTypes: ForcedTypes
        get() = configuration.generator.database.forcedTypes

    private val targetPackage: String
        get() = configuration.generator.target.packageName

    /**
     * A generic converter will be generated for this field, where a database type field is simply
     * encapsulated in the tiny type.
     */
    override infix fun String.withTinyType(tinyType: KClass<*>) {
        val field = tinyType.declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()
        registerForcedType(
            userClass = tinyType,
            converter = buildTinyTypeConverter(tinyType.simpleName, field)
        )
    }

    override infix fun String.withEnum(enum: KClass<out Enum<*>>) {
        registerForcedType(
            userClass = enum,
            converter = buildEnumConverter("$targetPackage.enums.${enum.simpleName}", enum)
        )
    }

    override infix fun String.withStringEnum(enum: KClass<out Enum<*>>) {
        registerForcedType(
            userClass = enum,
            converter = buildEnumConverter("String", enum)
        )
    }

    private fun buildTinyTypeConverter(userClass: String?, field: Method): String = when (field.returnType) {
        Instant::class -> "new ${InstantTinyTypeConverter::class.qualifiedName}<>(" +
            "$userClass.class, $userClass::new, $userClass::${field.name})"
        else -> "org.jooq.Converter.ofNullable(${field.returnType.canonicalName}.class, " +
            "$userClass.class, $userClass::new, $userClass::${field.name})"
    }

    private fun buildEnumConverter(type: String, enum: KClass<out Enum<*>>) =
        "new org.jooq.impl.EnumConverter<>($type.class, ${enum.simpleName}.class)"

    /**
     * The indicated converter will be used for this field. Used for fields where any conversion is needed
     * between the database type and the user type that is not covered in the other generators
     */
    override fun <T : Any, U : Any> String.withCustomConverter(converter: KClass<out Converter<T, U>>) =
        ConverterContextImpl(this, converter)

    inner class ConverterContextImpl<T : Any, U : Any>(
        private val name: String,
        private val converterClass: KClass<out Converter<T, U>>
    ) : ConverterContext<T, U> {

        private var fromType: KClass<T>? = null
        private var toType: KClass<U>? = null

        override fun from(fromType: KClass<T>): ConverterContext<T, U> = apply {
            if (this.fromType != null) throw IllegalArgumentException("from is already defined")
            this.fromType = fromType
            if (toType != null) register()
        }

        override fun to(toType: KClass<U>): ConverterContext<T, U> = apply {
            if (this.toType != null) throw IllegalArgumentException("to is already defined")
            this.toType = toType
            if (fromType != null) register()
        }

        private fun register() {
            name.registerForcedType(
                userClass = toType!!,
                converter = converterClass.qualifiedName
            )
        }
    }

    private fun String.registerForcedType(userClass: KClass<*>, converter: String?) {
        forcedTypes += ForcedType().also {
            it.expression = ".*\\.$name\\.$this"
            it.userType = userClass.qualifiedName
            it.converter = converter
        }
    }
}
