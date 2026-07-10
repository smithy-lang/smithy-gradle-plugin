description = "Integration test utilities used across multiple plugins."

plugins {
    id("smithy-gradle-plugin.java-conventions")
}

dependencies {
    implementation(gradleTestKit())
    implementation("org.junit.jupiter:junit-jupiter-api:6.1.1")
    implementation("org.junit.jupiter:junit-jupiter-engine:6.1.1")
    implementation("org.junit.jupiter:junit-jupiter-params:6.1.1")
    implementation("org.hamcrest:hamcrest:3.0")
}
