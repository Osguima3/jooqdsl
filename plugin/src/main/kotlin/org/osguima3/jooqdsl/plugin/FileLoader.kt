package org.osguima3.jooqdsl.plugin

import javafx.fxml.LoadException
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths
import javax.script.ScriptEngineManager

class FileLoader(classLoader: ClassLoader? = Thread.currentThread().contextClassLoader) {

    private val engine = ScriptEngineManager(classLoader).getEngineByExtension("kts")

    inline fun <reified T> loadScript(file: String): T = { eval(file) }() as T

    fun readFile(file: String) = Files.newBufferedReader(Paths.get(file))

    fun eval(file: String) = eval(readFile(file))

    fun eval(reader: Reader): Any = try {
        engine.eval(reader)
    } catch (e: Exception) {
        throw LoadException("Cannot eval script", e)
    }
}
