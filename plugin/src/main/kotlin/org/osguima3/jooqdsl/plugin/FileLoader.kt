/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 */

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