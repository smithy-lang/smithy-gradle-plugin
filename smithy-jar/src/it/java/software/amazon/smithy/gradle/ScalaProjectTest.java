package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class ScalaProjectTest {
    @Test
    public void testScalaProject() {
        Utils.withCopy("jar-plugin/scala-project", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/scala-project/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/scala-project/source/model/model.json",
                    "build/smithyprojections/scala-project/source/sources/main.smithy",
                    "build/smithyprojections/scala-project/source/sources/manifest",
                    "build/libs/scala-project.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/scala-project.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy",
                    "example/Main.class"
            );
        });
    }
}
