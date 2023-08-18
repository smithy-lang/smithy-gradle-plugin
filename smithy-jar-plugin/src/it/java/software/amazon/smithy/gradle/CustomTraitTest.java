package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CustomTraitTest {
    @Test
    public void testCustomTrait() {
        Utils.withCopy("jar-plugin/custom-trait", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            // Check that nested build and validation tasks succeeded
            assertSame(requireNonNull(result.task(":custom-string-trait:smithyBuild")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":custom-string-trait:smithyJarValidate")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":custom-string-trait:jar")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":custom-string-trait:smithyBuild")).getOutcome(), TaskOutcome.SUCCESS);


            // Check that custom trait artifacts were generated
            Utils.assertArtifactsCreated(buildDir,
                    "custom-string-trait/build/smithyprojections/custom-string-trait/source/build-info/smithy-build-info.json",
                    "custom-string-trait/build/smithyprojections/custom-string-trait/source/sources/custom-trait.smithy",
                    "custom-string-trait/build/smithyprojections/custom-string-trait/source/sources/manifest",
                    "custom-string-trait/build/libs/custom-string-trait.jar");

            // Check that the trait library jar contains expected files
            Utils.assertJarContains(buildDir, "custom-string-trait/build/libs/custom-string-trait.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/custom-trait.smithy",
                    "META-INF/services/software.amazon.smithy.model.traits.TraitService",
                    "io/smithy/examples/traits/JsonNameTrait.class",
                    "io/smithy/examples/traits/JsonNameTrait$Provider.class");

            Utils.assertArtifactsCreated(buildDir,
                    "consumer/build/smithyprojections/consumer/source/build-info/smithy-build-info.json",
                    "consumer/build/smithyprojections/consumer/source/sources/main.smithy",
                    "consumer/build/smithyprojections/consumer/source/sources/manifest");
        });
    }
}
