package io.github.osguima3.jooqdsl.maven

import org.apache.maven.cli.MavenCli
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path

class MavenPluginIntegrationTest {

    private val projectVersion = System.getProperty("project.version")

    @Test
    fun `maven can find the plugin`(@TempDir tempDir: Path) =
        runTest(
            tempDir,
            "simple-java",
            "help:describe",
            "-Dplugin=io.github.osguima3.jooqdsl:jooqdsl-maven-plugin"
        ) {
            assertThat(exitCode).withFailMessage { "Task failed: $output" }.isEqualTo(0)
            assertThat(output).contains("jooqdsl-maven-plugin")
            assertThat(output).contains("generate-jooq")
        }

    @Test
    fun `simple-java project code generation works`(@TempDir tempDir: Path) =
        runTest(tempDir, "simple-java", "clean", "test") {
            assertThat(exitCode).withFailMessage { "Task failed: $output" }.isEqualTo(0)
            assertThat(output).contains("BUILD SUCCESS")
            assertThat(output).contains("generate-jooq")

            val regex = """public final TableField<TestRecord, (.+?)> (\w+) =""".toRegex()
            val actual = getTableFile("io/github/osguima3/jooqdsl/it/simplejava/model", "Test.java")
            val fields = actual.mapNotNull { regex.find(it.trim())?.destructured }
                .map { (fieldType, fieldName) -> fieldType to fieldName }
            assertThat(fields)
                .withFailMessage { "Fields not found. File content: ${actual.joinToString("\n")}" }
                .isNotEmpty

            assertThat(fields).containsExactly(
                "Integer" to "INT",
                "String" to "STRING",
                "BigDecimal" to "BIG_DECIMAL",
                "StringValueObject" to "VALUE_OBJECT",
                "InstantValueObject" to "INSTANT_OBJECT",
                "String" to "JSON",
                "CustomEnum" to "CUSTOM_ENUM",
                "StringEnum" to "STRING_ENUM",
                "DateValueObject" to "COMPOSITE",
                "Date" to "CONVERTER",
                "String" to "CUSTOM",
            )
        }

    @Test
    fun `multi-module project code generation works`(@TempDir tempDir: Path) =
        runTest(tempDir, "multi-module", "clean", "test") {
            assertThat(exitCode).withFailMessage { "Task failed: $output" }.isEqualTo(0)
            assertThat(output).contains("BUILD SUCCESS")
            assertThat(output).contains("generate-jooq")

            val regex = """val (\w+): TableField<TestRecord, (.+?)\?> =""".toRegex()
            val actual = getTableFile("io/github/osguima3/jooqdsl/it/multimodule/app/model", "Test.kt", "app")
            val fields = actual.mapNotNull { regex.find(it.trim())?.destructured }
                .map { (fieldName, fieldType) -> fieldType to fieldName }
            assertThat(fields)
                .withFailMessage { "Fields not found. File content: ${actual.joinToString("\n")}" }
                .isNotEmpty

            assertThat(fields).containsExactly(
                "Int" to "INT",
                "String" to "STRING",
                "BigDecimal" to "BIG_DECIMAL",
                "StringValueObject" to "VALUE_OBJECT",
                "InstantValueObject" to "INSTANT_OBJECT",
                "String" to "JSON",
                "CustomEnum" to "CUSTOM_ENUM",
                "StringEnum" to "STRING_ENUM",
                "DateValueObject" to "COMPOSITE",
                "Date" to "CONVERTER",
                "String" to "CUSTOM",
            )
        }

    fun runTest(tempDir: Path, testProjectName: String, vararg goals: String, block: MavenResult.() -> Unit) {
        val sourceDir = File(
            javaClass.getResource("/it/$testProjectName")?.toURI()
                ?: error("IT project not found: $testProjectName")
        )

        val workingDir = tempDir.resolve(testProjectName).toFile()
        sourceDir.copyRecursively(workingDir, overwrite = true)
        replacePlaceholders(workingDir)
        val result = runMaven(workingDir, goals)
        block(result)
    }

    private fun replacePlaceholders(directory: File) = directory.walkTopDown().filter { it.name == "pom.xml" }
        .forEach { it.writeText(it.readText().replace("@project.version@", projectVersion)) }

    private fun runMaven(workingDir: File, args: Array<out String>): MavenResult {
        System.setProperty("maven.multiModuleProjectDirectory", workingDir.absolutePath)

        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val exitCode = MavenCli().doMain(
            args,
            workingDir.absolutePath,
            PrintStream(outputStream, true, "UTF-8"),
            PrintStream(outputStream, true, "UTF-8")
        )

        return MavenResult(
            workingDir = workingDir,
            exitCode = exitCode,
            output = outputStream.toString("UTF-8"),
            error = errorStream.toString("UTF-8"),
        )
    }

    private fun MavenResult.getTableFile(packagePath: String, fileName: String, subModule: String = ""): List<String> {
        val modulePrefix = if (subModule.isNotEmpty()) "$subModule/" else ""
        val packageFolder = workingDir.resolve("${modulePrefix}target/generated-sources/jooq/$packagePath")
        val file = packageFolder.resolve("tables/$fileName")
        assertThat(file).withFailMessage {
            "Expected generated file not found: ${file.absolutePath}\n Generated files:\n" +
                packageFolder.walkTopDown().filter { it.isFile }.joinToString("\n") { it.absolutePath }
        }.exists().isFile()

        return file.readText().lines()
    }

    data class MavenResult(
        val workingDir: File,
        val exitCode: Int,
        val output: String,
        val error: String,
    )
}
