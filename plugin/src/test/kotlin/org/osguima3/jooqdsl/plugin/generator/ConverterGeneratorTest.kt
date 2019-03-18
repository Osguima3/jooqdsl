package org.osguima3.jooqdsl.plugin.generator

import org.assertj.core.api.Assertions.assertThat
import org.jooq.codegen.ConverterGenerator
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

class ConverterGeneratorTest {

    private val converterGenerator = ConverterGenerator()

    @Test
    @Disabled
    fun testWrite() {
        val resource = ConverterGeneratorTest::class.java.classLoader.getResource(".")
        val converterFile = File(resource.toURI().path, "TestConverter.java")
//        converterGenerator.generateConverter(converterFile, "org.company.myproject", "TestConverter")

        assertThat(converterFile).exists()
    }
}