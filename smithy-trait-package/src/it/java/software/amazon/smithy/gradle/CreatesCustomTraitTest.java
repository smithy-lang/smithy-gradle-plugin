package software.amazon.smithy.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class CreatesCustomTraitTest {

    @Test
    public void createsTraitsAndAddsToJar() {
        Utils.withCopy("trait-package-plugin/create-simple-trait", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertJarContains(buildDir, "build/libs/create-simple-trait-9.9.9.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/custom-trait.smithy",
                    "META-INF/services/software.amazon.smithy.model.traits.TraitService",
                    "io/smithy/gradle/examples/traits/ResourceMetadataTrait.class",
                    "io/smithy/gradle/examples/traits/ResourceType.class",
                    "io/smithy/gradle/examples/traits/JsonNameTrait.class"
            );

            String spiContents = Utils.getJarEntryContents(new File(buildDir, "build/libs/create-simple-trait-9.9.9.jar"),
                    "META-INF/services/software.amazon.smithy.model.traits.TraitService");

            assertTrue(spiContents.contains("io.smithy.gradle.examples.traits.ResourceMetadataTrait$Provider"));
            assertTrue(spiContents.contains("io.smithy.gradle.examples.traits.JsonNameTrait$Provider"));
        });
    }
}
