dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.jooq)

    // Test dependencies
    testImplementation(libs.bundles.testing)

    testRuntimeOnly(libs.junit.jupiter.engine)
}
