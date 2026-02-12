dependencies {
    implementation(project(":jooqdsl-model"))
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.kotlin.scripting)
    implementation(libs.bundles.jooq)

    // Test dependencies
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
