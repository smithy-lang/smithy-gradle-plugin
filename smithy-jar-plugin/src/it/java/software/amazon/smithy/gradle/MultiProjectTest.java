package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MultiProjectTest {
    @Test
    public void testProjection() {
        Utils.withCopy("jar-plugin/multi-project", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            // Check that expected smithy task executed for consumer
            assertSame(requireNonNull(result.task(":consumer:smithyBuild")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":consumer:smithyJarValidate")).getOutcome(), TaskOutcome.SUCCESS);

            // Check that producer jars created
            Utils.assertArtifactsCreated(buildDir,
                    "producer1/build/libs/producer1-999.999.999.jar",
                    "producer2/build/libs/producer2-999.999.999.jar");

            // Check that all smithy and jar artifacts for consumer created
            Utils.assertArtifactsCreated(buildDir,
                    "consumer/build/smithyprojections/consumer/source/build-info/smithy-build-info.json",
                    "consumer/build/smithyprojections/consumer/source/sources/main.smithy",
                    "consumer/build/smithyprojections/consumer/source/sources/manifest",
                    "consumer/build/libs/consumer-999.999.999.jar");

            // Check that that jar contains correct objects
            Utils.assertJarContains(buildDir, "consumer/build/libs/consumer-999.999.999.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");

            // Check that correct tags were added to jar manifest
            JarFile jar = new JarFile(new File(buildDir, "consumer/build/libs/consumer-999.999.999.jar"));
            Manifest manifest = jar.getManifest();
            String tags = (String) manifest.getMainAttributes().get(new Attributes.Name("Smithy-Tags"));
            jar.close();
            String[] tagValues = tags.split(", ");

            assertThat(Arrays.asList(tagValues), containsInAnyOrder(
                    "software.amazon.smithy.it:consumer", "software.amazon.smithy.it:consumer:999.999.999",
                    "software.amazon.smithy.it"));
        });
    }
}
