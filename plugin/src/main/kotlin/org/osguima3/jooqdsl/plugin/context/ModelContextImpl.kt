package org.osguima3.jooqdsl.plugin.context

import org.jetbrains.kotlin.com.intellij.openapi.util.io.FileUtil
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.ForcedType
import org.osguima3.jooqdsl.model.context.ModelContext
import org.osguima3.jooqdsl.model.context.TablesContext
import org.osguima3.jooqdsl.model.converter.Converter
import java.io.File
import kotlin.reflect.KClass

class ModelContextImpl(
    private val configuration: Configuration,
    private val generatedConverters: MutableSet<String> = mutableSetOf()
) : ModelContext {

    internal val converterPackage = Converter::class.java.`package`.name

    internal val targetPackage get() = configuration.generator.target.packageName

    private val tablesContext = TablesContextImpl(this)

    private val converterFolder = converterPackage.replace('.', '/')

    private val forcedTypes get() = configuration.generator.database.forcedTypes

    private val targetDirectory get() = configuration.generator.target.directory

    override fun tables(block: TablesContext.() -> Unit) = tablesContext.block()

    internal fun registerForcedType(expression: String, userType: KClass<*>, converter: String) = apply {
        forcedTypes += ForcedType().also {
            it.expression = expression
            it.userType = userType.qualifiedName
            it.converter = converter
        }
    }

    internal fun generateConverter(converterName: String) {
        if (generatedConverters.add(converterName)) {
            val source = this::class.java.classLoader.getResource("converter/$converterName.java")
            val target = File("$targetDirectory/$converterFolder").run {
                mkdirs()
                File("$absolutePath/$converterName.java")
            }
            println("Copying from $source to $target")
            FileUtil.copy(source.openStream(), target.outputStream())
        }
    }
}
