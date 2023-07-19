package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class KotlinJvmProjectTest {
    @Test
    public void testSourceProjection() {
        Utils.withCopy("kotlin-jvm-project", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/kotlin-jvm-project/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/kotlin-jvm-project/source/model/model.json",
                    "build/smithyprojections/kotlin-jvm-project/source/sources/main.smithy",
                    "build/smithyprojections/kotlin-jvm-project/source/sources/manifest",
                    "build/libs/kotlin-jvm-project.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/kotlin-jvm-project.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy",
                    "META-INF/kotlin-jvm-project.kotlin_module",
                    "MainKt.class"
            );
        });
    }
}
