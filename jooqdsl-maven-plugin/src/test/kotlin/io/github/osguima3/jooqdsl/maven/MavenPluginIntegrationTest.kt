package io.github.osguima3.jooqdsl.maven

import org.apache.maven.cli.MavenCli
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

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
            assertThat(exitCode).isEqualTo(0)
            assertThat(output).contains("jooqdsl-maven-plugin")
            assertThat(output).contains("generate-jooq")
        }

    @Test
    fun `simple-java project code generation works`(@TempDir tempDir: Path) =
        runTest(tempDir, "simple-java", "clean", "generate-sources") {
            assertThat(exitCode).isEqualTo(0)
            assertThat(output).contains("BUILD SUCCESS")
            assertThat(output).contains("generate-jooq")

            assertThat(getTableFields("io/github/osguima3/jooqdsl/it/simplejava/model"))
                .contains("java.util.UUID" to "UUID")
                .contains("String" to "STRING")
                .contains("Instant" to "INSTANT")
                .contains("Integer" to "INT")
                .contains("BigDecimal" to "BIG_DECIMAL")
                .contains("CustomEnum" to "CUSTOM_ENUM")
                .contains("StringEnum" to "STRING_ENUM")
                .contains("Date" to "CONVERTER")
                .contains("String" to "CUSTOM")
        }

    @Test
    fun `multi-module project code generation works`(@TempDir tempDir: Path) =
        runTest(tempDir, "multi-module", "clean", "generate-sources") {
            assertThat(exitCode).isEqualTo(0)
            assertThat(output).contains("BUILD SUCCESS")
            assertThat(output).contains("generate-jooq")

            assertThat(getTableFields("io/github/osguima3/jooqdsl/it/multimodule/app/model", "app"))
                .contains("IdValueObject" to "UUID")
                .contains("StringValueObject" to "STRING")
                .contains("InstantValueObject" to "INSTANT")
                .contains("IntValueObject" to "INT")
                .contains("BigDecimalValueObject" to "BIG_DECIMAL")
                .contains("CustomEnum" to "CUSTOM_ENUM")
                .contains("StringEnum" to "STRING_ENUM")
                .contains("DateValueObject" to "VALUE_OBJECT")
                .contains("Date" to "CONVERTER")
                .contains("String" to "CUSTOM")
        }

    fun runTest(tempDir: Path, testProjectName: String, vararg goals: String, block: MavenResult.() -> Unit) {
        val sourceDir = File(
            javaClass.getResource("/it/$testProjectName")?.toURI()
                ?: error("IT project not found: $testProjectName")
        )

        val workingDir = tempDir.resolve(testProjectName).toFile()
        copyDirectory(sourceDir, workingDir)
        replacePlaceholders(workingDir)
        val result = runMaven(workingDir, goals)
        block(result)
    }

    private fun copyDirectory(source: File, target: File) {
        if (!source.exists()) {
            throw IllegalArgumentException("Source directory does not exist: ${source.absolutePath}")
        }

        target.mkdirs()

        source.walkTopDown().forEach { file ->
            val relativePath = file.relativeTo(source)
            val targetFile = target.resolve(relativePath)

            when {
                file.isDirectory -> targetFile.mkdirs()
                file.isFile -> {
                    targetFile.parentFile.mkdirs()
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    private fun replacePlaceholders(directory: File) {
        directory.walkTopDown()
            .filter { it.isFile && it.name == "pom.xml" }
            .forEach { pomFile ->
                val content = pomFile.readText()
                val updated = content.replace("@project.version@", projectVersion)
                pomFile.writeText(updated)
            }
    }

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

    fun MavenResult.getTableFields(packagePath: String, subModule: String = ""): List<Pair<String, String>> {
        val modulePrefix = if (subModule.isNotEmpty()) "$subModule/" else ""
        val file = workingDir.resolve("${modulePrefix}target/generated-sources/jooq/$packagePath/tables/Test.java")
        assertThat(file).exists().isFile()

        val regex = """public final TableField<TestRecord, (.+?)> (\w+) =""".toRegex()

        return file.readText().lines()
            .mapNotNull { regex.find(it.trim())?.destructured }
            .map { (fieldType, fieldName) -> fieldType to fieldName }
    }

    data class MavenResult(
        val workingDir: File,
        val exitCode: Int,
        val output: String,
        val error: String,
    )
}
