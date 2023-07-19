package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class MultipleJarsTest {
    @Test
    public void addsSmithyTagsToJars() {
        Utils.withCopy("multiple-jars", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);

            // Check that all projections and expected jars exist
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/multiple-jars/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/multiple-jars/source/model/model.json",
                    "build/smithyprojections/multiple-jars/source/sources/main.smithy",
                    "build/smithyprojections/multiple-jars/source/sources/manifest",
                    "build/smithyprojections/multiple-jars/sourceJar/build-info/smithy-build-info.json",
                    "build/smithyprojections/multiple-jars/sourceJar/model/model.json",
                    "build/smithyprojections/multiple-jars/sourceJar/sources/model.json",
                    "build/smithyprojections/multiple-jars/sourceJar/sources/manifest",
                    "build/libs/multiple-jars.jar",
                    "build/libs/multiple-jars-sources.jar");

            // Check that models were correctly staged for both staging tasks
            Utils.assertArtifactsCreated(buildDir,
            "build/tmp/staging-smithyJarStaging/META-INF/smithy/main.smithy",
                    "build/tmp/staging-smithyJarStaging/META-INF/smithy/manifest",
                    "build/tmp/staging-stageSmithySources/META-INF/smithy/model.json",
                    "build/tmp/staging-stageSmithySources/META-INF/smithy/manifest"
            );

            Utils.assertJarContains(buildDir, "build/libs/multiple-jars-sources.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/model.json");

            JarFile jar = new JarFile(new File(buildDir, "build/libs/multiple-jars-sources.jar"));
            Manifest manifest = jar.getManifest();
            String tags = (String) manifest.getMainAttributes().get(new Attributes.Name("Smithy-Tags"));
            jar.close();
            String[] tagValues = tags.split(", ");

            assertThat(Arrays.asList(tagValues), containsInAnyOrder("a", "b", "c"));
        });
    }
}
