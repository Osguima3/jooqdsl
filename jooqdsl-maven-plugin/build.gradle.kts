// Generate Maven plugin descriptor using Maven Wrapper
val generateMavenPluginDescriptor by tasks.registering(Exec::class) {
    dependsOn(tasks.named("classes"))

    description = "Generate Maven plugin descriptor using maven-plugin-plugin"
    group = "build"

    // Load template from resources and replace placeholders
    doFirst {
        val template = file("src/main/resources/descriptor-pom.xml.template").readText()
        val descriptorPom = file("${layout.buildDirectory.get().asFile}/descriptor-pom.xml")

        descriptorPom.parentFile.mkdirs()
        descriptorPom.writeText(
            template
                .replace("@project.group@", project.group.toString())
                .replace("@project.name@", project.name)
                .replace("@project.version@", project.version.toString())
                .replace("@build.output.directory@", "${layout.buildDirectory.get().asFile}/classes/kotlin/main")
        )
    }

    workingDir = layout.buildDirectory.get().asFile
    commandLine = listOf(
        rootProject.file(if (System.getProperty("os.name").lowercase().contains("windows")) "mvnw.cmd" else "mvnw").absolutePath,
        "-f", "descriptor-pom.xml",
        "org.apache.maven.plugins:maven-plugin-plugin:3.15.0:descriptor"
    )
}

tasks.named("jar") {
    dependsOn(generateMavenPluginDescriptor)
}

dependencies {
    implementation(project(":jooqdsl-model"))
    implementation(project(":jooqdsl-core"))
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.kotlin.scripting)
    implementation(libs.bundles.jooq)

    compileOnly(libs.bundles.maven.plugin)
    compileOnly(libs.bundles.testcontainers)

    implementation(libs.mojo.executor)
    implementation(libs.jakarta.xml.bind.api)

    // Test dependencies
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.jackson)
    testImplementation(libs.bundles.maven.test)
    testImplementation(libs.testcontainers.postgresql)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.slf4j.simple)
}

// Configure test task to publish to maven local first for integration tests
tasks.test {
    dependsOn("publishToMavenLocal")

    // Capture and display test output
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
    }

    // Pass project version to tests
    systemProperty("project.version", project.version)
}
