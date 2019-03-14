package org.osguima3

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileWriter
import java.io.IOException

@Mojo(name = "hello")
class HelloMojo : AbstractMojo() {

    @Parameter(property = "msg", defaultValue = "from maven")
    var msg: String? = null

    override fun execute() {
        getLog().info("Hello " + msg!!)
    }
}
