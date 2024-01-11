package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class DisableJarTest {
    @Test
    public void testProjection() {
        Utils.withCopy("jar-plugin/disable-jar", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/disable-jar/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/disable-jar/source/model/model.json",
                    "build/smithyprojections/disable-jar/source/sources/main.smithy",
                    "build/smithyprojections/disable-jar/source/sources/manifest");
            Utils.assertArtifactsNotCreated(buildDir, "build/libs/disable-jar.jar");
        });
    }
}
