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

import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment
import org.twdata.maven.mojoexecutor.MojoExecutor.artifactId
import org.twdata.maven.mojoexecutor.MojoExecutor.configuration
import org.twdata.maven.mojoexecutor.MojoExecutor.element
import org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo
import org.twdata.maven.mojoexecutor.MojoExecutor.goal
import org.twdata.maven.mojoexecutor.MojoExecutor.groupId
import org.twdata.maven.mojoexecutor.MojoExecutor.plugin
import org.twdata.maven.mojoexecutor.MojoExecutor.version

typealias AdditionalSources = List<String>

fun AdditionalSources.precompile(environment: ExecutionEnvironment) {
    precompileKotlin(this, environment)
    precompileJava(this, environment)
}

private fun precompileKotlin(sources: AdditionalSources, environment: ExecutionEnvironment) {
    executeMojo(
        plugin(
            groupId("org.jetbrains.kotlin"),
            artifactId("kotlin-maven-plugin"),
            version(KotlinVersion.CURRENT.toString())
        ),
        goal("compile"),
        configuration(
            element("jvmTarget", environment.mavenProject.properties.getProperty("maven.compiler.target", "17")),
            element("sourceDirs", *sources
                .map { element("sourceDir", it) }
                .toTypedArray())
        ),
        environment
    )
}

private fun precompileJava(sources: AdditionalSources, environment: ExecutionEnvironment) {
    executeMojo(
        plugin(
            groupId("org.apache.maven.plugins"),
            artifactId("maven-compiler-plugin"),
            version("3.11.0")
        ),
        goal("compile"),
        configuration(
            element("includes", *sources
                .map { element("include", "$it/**/*.java") }
                .toTypedArray()
            )
        ),
        environment
    )
}
