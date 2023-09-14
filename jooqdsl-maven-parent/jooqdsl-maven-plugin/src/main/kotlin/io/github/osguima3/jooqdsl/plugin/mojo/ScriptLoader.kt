/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
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

package io.github.osguima3.jooqdsl.plugin.mojo

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import java.io.File
import java.io.Reader
import javax.script.ScriptEngineManager

class ScriptLoader {

    init {
        setIdeaIoUseFallback()
    }

    private val engine = ScriptEngineManager(Thread.currentThread().contextClassLoader)
        .getEngineByExtension("kts") ?: throw MojoFailureException("KTS engine not found")

    inline fun <reified T> loadScript(file: File): T = eval(file.bufferedReader()) as T

    inline fun <reified T> loadScript(reader: Reader): T = eval(reader) as T

    fun eval(reader: Reader): Any = try {
        engine.eval(reader)
    } catch (e: Exception) {
        throw MojoExecutionException("Cannot eval script", e)
    }
}
