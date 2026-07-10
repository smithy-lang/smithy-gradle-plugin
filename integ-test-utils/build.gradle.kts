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
