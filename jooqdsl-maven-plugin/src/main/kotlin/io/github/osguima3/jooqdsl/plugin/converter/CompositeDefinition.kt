package io.github.osguima3.jooqdsl.plugin.converter

data class CompositeDefinition(private val components: List<NullableConverterDefinition>) : ForcedTypeDefinition {

    constructor(vararg components: NullableConverterDefinition) : this(components.asList())

    override val userType = components.last().userType

    override val converter =
        "org.jooq.Converters.of(${components.joinToString(", ", transform = ForcedTypeDefinition::converter)})"
}
