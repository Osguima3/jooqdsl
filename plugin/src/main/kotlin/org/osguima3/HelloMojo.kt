package org.osguima3

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = "hello")
class HelloMojo : AbstractMojo() {

    @Parameter(property = "msg", defaultValue = "from maven")
    lateinit var msg: String

    override fun execute() {
        log.info("Hello $msg")
    }
}
