package org.osguima3.jooqdsl.plugin.visitor

import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.osguima3.jooqdsl.model.converter.Converter
import org.osguima3.jooqdsl.model.converter.InstantTinyTypeConverter
import org.osguima3.jooqdsl.model.definition.ConverterContext
import org.osguima3.jooqdsl.model.definition.TableContext
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
        val userClass = tinyType.simpleName
        val dbClass = field.returnType.canonicalName
        registerForcedType(
            userClass = tinyType,
            converter = "org.jooq.Converter.ofNullable($dbClass.class, $userClass.class, " +
                "$userClass::new, $userClass::${field.name})"
        )
    }

    override infix fun String.withInstantTinyType(tinyType: KClass<*>) {
        val field = tinyType.declaredMemberProperties.mapNotNull(KProperty<*>::javaGetter).single()
        val userClass = tinyType.simpleName
        registerForcedType(
            userClass = tinyType,
            converter = "new ${InstantTinyTypeConverter::class.qualifiedName}<>($userClass.class, " +
                "$userClass::new, $userClass::${field.name})"
        )
    }

    override infix fun String.withStringEnum(enum: KClass<out Enum<*>>) {
        registerForcedType(
            userClass = enum,
            converter = "new org.jooq.impl.EnumConverter<>(String.class, ${enum.simpleName}.class)"
        )
    }

    override infix fun String.withEnum(enum: KClass<out Enum<*>>) {
        registerForcedType(
            userClass = enum,
            converter = "new org.jooq.impl.EnumConverter<>(" +
                "$targetPackage.enums.${enum.simpleName}.class, ${enum.simpleName}.class)"
        )
    }

    /**
     * The indicated converter will be used for this field. Used for fields where any conversion is needed
     * between the database type and the user type that is not covered in the other generators
     */
    override fun <T : Any, U : Any> String.withCustomConverter(converterClass: KClass<out Converter<T, U>>) =
        ConverterContextImpl(this, converterClass)

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
