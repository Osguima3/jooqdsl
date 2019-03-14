package org.osguima3

import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.junit.jupiter.api.Test


class HelloMojoTest : AbstractMojoTestCase() {


    protected override fun setUp() {
        super.setUp()
    }


    protected override fun tearDown() {
        super.tearDown()
    }


    @Test
    fun testSomething() {
        val pom = getTestFile("src/test/resources/unit/project-to-test/pom.xml")
//        assertNotNull(pom)
        assertTrue(pom.exists())

        val myMojo = lookupMojo("hello", pom) as HelloMojo
//        assertNotNull(myMojo)
        myMojo.execute()
    }
}
