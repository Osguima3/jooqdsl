package io.osguima3.jooqdsl.plugin.configuration

import org.twdata.maven.mojoexecutor.MojoExecutor

typealias AdditionalSources = List<String>

fun AdditionalSources.precompile(environment: MojoExecutor.ExecutionEnvironment) {
    precompileKotlin(this, environment)
    precompileJava(this, environment)
}

private fun precompileKotlin(sources: AdditionalSources, environment: MojoExecutor.ExecutionEnvironment) {
    MojoExecutor.executeMojo(
        MojoExecutor.plugin(
            MojoExecutor.groupId("org.jetbrains.kotlin"),
            MojoExecutor.artifactId("kotlin-maven-plugin"),
            MojoExecutor.version(KotlinVersion.CURRENT.toString())
        ),
        MojoExecutor.goal("compile"),
        MojoExecutor.configuration(
            MojoExecutor.element("sourceDirs", *sources
                .map { MojoExecutor.element("sourceDir", it) }
                .toTypedArray())
        ),
        environment
    )
}

private fun precompileJava(sources: AdditionalSources, environment: MojoExecutor.ExecutionEnvironment) {
    MojoExecutor.executeMojo(
        MojoExecutor.plugin(
            MojoExecutor.groupId("org.apache.maven.plugins"),
            MojoExecutor.artifactId("maven-compiler-plugin"),
            MojoExecutor.version("3.8.0")
        ),
        MojoExecutor.goal("compile"),
        MojoExecutor.configuration(
            MojoExecutor.element("includes", *sources
                .map { MojoExecutor.element("include", "$it/**/*.java") }
                .toTypedArray()
            )
        ),
        environment
    )
}
