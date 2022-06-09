package io.github.osguima3.jooqdsl.plugin.configuration

import io.github.osguima3.jooqdsl.model.ModelDefinition
import io.github.osguima3.jooqdsl.plugin.mojo.ScriptLoader
import java.io.File

typealias DefinitionFile = String

fun DefinitionFile.loadDefinition(path: File): ModelDefinition =
    ScriptLoader().loadScript(File(path, this))
