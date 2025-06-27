package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.Node;

public class SelectTaskTest {
    @Test
    public void selectsString() {
        Utils.withCopy("base-plugin/uses-explicitly-set-cli-version", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("select", "--selector", "member")
                    .build();

            Utils.assertSmithyBuildDidNotRun(result);

            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/uses-explicitly-set-cli-version/source/build-info/smithy-build-info.json");

            Assertions.assertTrue(result.getOutput().contains("smithy.example#Foo$bar"));
        });
    }

    @Test
    public void selectsWithShowString() {
        Utils.withCopy("base-plugin/uses-explicitly-set-cli-version", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("select", "--selector", "member", "--show", "type,vars")
                    .build();

            Utils.assertSmithyBuildDidNotRun(result);

            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/uses-explicitly-set-cli-version/source/build-info/smithy-build-info.json");

            Assertions.assertTrue(result.getOutput().contains("\"shape\": \"smithy.example#Foo$bar\""));
        });
    }

    @Test
    public void selectsWithShowTraitsString() {
        Utils.withCopy("base-plugin/uses-explicitly-set-cli-version", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("select", "--selector", "structure > member", "--show-traits", "documentation", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildDidNotRun(result);

            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/uses-explicitly-set-cli-version/source/build-info/smithy-build-info.json");

            Assertions.assertTrue(result.getOutput().contains("\"smithy.api#documentation\": \"a string member\""));
        });
    }

    @Test
    public void selectUsesGradleClasspath() {
        Utils.withCopy("base-plugin/output-directory", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("select", "--selector", "operation[trait|aws.auth#unsignedPayload]")
                    .build();

            Utils.assertSmithyBuildDidNotRun(result);
            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/output-directory/source/build-info/smithy-build-info.json");

            Assertions.assertTrue(result.getOutput().contains("smithy.example#Foo"));
        });
    }
}
