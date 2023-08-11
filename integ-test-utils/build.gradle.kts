description = "Integration test utilities used across multiple plugins."

dependencies {
    implementation(gradleTestKit())
    implementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.4.0")
    implementation("org.junit.jupiter:junit-jupiter-params:5.4.0")
    implementation("org.hamcrest:hamcrest:2.1")
}
