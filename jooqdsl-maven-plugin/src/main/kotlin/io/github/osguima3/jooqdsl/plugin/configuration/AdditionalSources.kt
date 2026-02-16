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

package io.github.osguima3.jooqdsl.plugin.configuration

import org.apache.maven.project.MavenProject
import org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment
import org.twdata.maven.mojoexecutor.MojoExecutor.configuration
import org.twdata.maven.mojoexecutor.MojoExecutor.element
import org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo
import org.twdata.maven.mojoexecutor.MojoExecutor.goal
import org.twdata.maven.mojoexecutor.MojoExecutor.plugin
import java.util.Properties

typealias AdditionalSources = List<String>

fun AdditionalSources.precompile(environment: ExecutionEnvironment) {
    environment.precompileKotlin(this)
    environment.precompileJava(this)
}

private fun ExecutionEnvironment.precompileKotlin(sourceRoots: AdditionalSources) {
    val javaVersion = mavenProject.properties.getProperty("maven.compiler.target") ?: JavaVersion.current().toString()
    val originalSourceRoots = mavenProject.replaceSourceRoots(sourceRoots)

    executeMojo(
        plugin("org.jetbrains.kotlin", "kotlin-maven-plugin", KotlinVersion.CURRENT.toString()),
        goal("compile"),
        configuration(
            element("jvmTarget", javaVersion),
            element(
                "sourceDirs",
                *sourceRoots.map { element("sourceDir", it) }.toTypedArray()
            )
        ),
        this,
    )

    mavenProject.replaceSourceRoots(originalSourceRoots)
}

private fun MavenProject.replaceSourceRoots(sourceRoots: List<String>): List<String> {
    val currentSourceRoots = compileSourceRoots.toList()
    compileSourceRoots.clear()
    compileSourceRoots.addAll(sourceRoots)
    return currentSourceRoots
}

private fun ExecutionEnvironment.precompileJava(sources: AdditionalSources) {
    val mavenPluginsVersion = loadMavenPluginsVersion()

    executeMojo(
        plugin("org.apache.maven.plugins", "maven-compiler-plugin", mavenPluginsVersion),
        goal("compile"),
        configuration(
            element("includes", *sources.map { element("include", "$it/**/*.java") }.toTypedArray())
        ),
        this,
    )
}

private fun loadMavenPluginsVersion(): String {
    val props = Properties()
    val inputStream = object {}.javaClass.classLoader.getResourceAsStream("maven-versions.properties")
        ?: throw IllegalStateException("maven-versions.properties not found in classpath")

    inputStream.use(props::load)
    return props.getProperty("maven.plugins.version")
        ?: throw IllegalStateException("maven.plugins.version not found in maven-versions.properties")
}
