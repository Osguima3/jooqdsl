package io.osguima3.jooqdsl.plugin.converter

data class CompositeForcedType(private val components: List<SimpleForcedType>) : IForcedType {

    constructor(vararg components: SimpleForcedType) : this(components.asList())

    override val userType = components.last().userType

    override val converter =
        "org.jooq.Converters.of(${components.joinToString(", ", transform = IForcedType::converter)})"
}
