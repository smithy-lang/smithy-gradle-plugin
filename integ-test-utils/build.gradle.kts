description = "Integration test utilities used across multiple plugins."

plugins {
    id("smithy-gradle-plugin.java-conventions")
}

dependencies {
    implementation(gradleTestKit())
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.jupiter.engine)
    implementation(libs.junit.jupiter.params)
    implementation(libs.hamcrest)
}

// Unlike the published plugins, this module's main sources are test-support helpers that
// depend on JUnit 6, so they must be compiled against Java 17 rather than the Java 8 default
// applied by the java-conventions plugin.
tasks.compileJava {
    options.release.set(17)
}
