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
