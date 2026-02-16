package org.jooq.codegen

import io.github.osguima3.jooqdsl.core.context.JooqContext
import io.github.osguima3.jooqdsl.core.context.ModelContextImpl
import io.github.osguima3.jooqdsl.core.converter.FieldDefinition
import io.github.osguima3.jooqdsl.core.converter.ForcedTypeDefinition
import io.github.osguima3.jooqdsl.core.converter.SkippedDefinition
import io.github.osguima3.jooqdsl.model.ModelDefinition
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator

class JooqConfigurator : JooqContext {

    private val forcedTypes = mutableMapOf<String, ForcedTypeDefinition>()

    fun load(modelDefinition: ModelDefinition) = modelDefinition.configure(ModelContextImpl(this))

    override fun configureField(tableName: String, fieldName: String, definition: FieldDefinition) = when (definition) {
        is SkippedDefinition -> Unit
        is ForcedTypeDefinition -> forcedTypes[".*\\.$tableName\\.$fieldName"] = definition
    }

    fun apply(configuration: Configuration) = configuration.apply {
        val language = generator.getLanguage()
        forcedTypes.forEach { (expression, definition) -> generator.addForcedType(definition, expression, language) }
    }

    private fun Generator.addForcedType(definition: ForcedTypeDefinition, expression: String, language: Language) =
        database.forcedTypes.add(definition.toForcedType(expression, target.packageName, language))

    private fun Generator.getLanguage(): Language {
        val generator = Class.forName(name).getDeclaredConstructor().newInstance() as AbstractGenerator
        val language = generator.language

        check(language in setOf(Language.JAVA, Language.KOTLIN)) {
            "$language generation is not supported by jOOQ DSL. Please use Java or Kotlin generation instead."
        }

        return language
    }
}