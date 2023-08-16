package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class SourceProjectionJarTest {
    @Test
    public void testSourceProjectionJar() {
        Utils.withCopy("source-projection-jar", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace", "--debug")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/source-projection-jar/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/source-projection-jar/source/model/model.json",
                    "build/smithyprojections/source-projection-jar/source/sources/main.smithy",
                    "build/smithyprojections/source-projection-jar/source/sources/manifest",
                    "build/smithyprojections/source-projection-jar/foo/build-info/smithy-build-info.json",
                    "build/smithyprojections/source-projection-jar/foo/model/model.json",
                    "build/smithyprojections/source-projection-jar/foo/sources/manifest",
                    "build/smithyprojections/source-projection-jar/foo/sources/model.json",
                    "build/libs/source-projection-jar.jar");
            Utils.assertJarContains(buildDir, "build/libs/source-projection-jar.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");
        });
    }
}
