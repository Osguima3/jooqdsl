plugins {
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("jooqDsl") {
            id = "io.github.osguima3.jooqdsl"
            implementationClass = "io.github.osguima3.jooqdsl.JooqDslPlugin"
        }
    }
}

dependencies {
    testImplementation(gradleTestKit())
}
