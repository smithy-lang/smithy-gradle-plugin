package software.amazon.smithy.gradle;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.loader.ModelAssembler;
import software.amazon.smithy.model.shapes.ShapeId;

import java.io.File;



public class BuildDependenciesTest {
    @Test
    public void buildsCorrectlyWithSmithyBuildDependencies() {
        Utils.withCopy("build-dependencies", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            // Check that nested build and validation tasks succeeded
            assertSame(requireNonNull(result.task(":internal-model:smithyBuild")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":internal-model:smithyJarValidate")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":internal-model:jar")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":service:smithyBuild")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":service:smithyJarValidate")).getOutcome(), TaskOutcome.SUCCESS);
            assertSame(requireNonNull(result.task(":service:jar")).getOutcome(), TaskOutcome.SUCCESS);

            // Check that internal model artifacts were generated
            Utils.assertArtifactsCreated(buildDir,
                    "internal-model/build/smithyprojections/internal-model/source/build-info/smithy-build-info.json",
                    "internal-model/build/smithyprojections/internal-model/source/sources/internal.smithy",
                    "internal-model/build/smithyprojections/internal-model/source/sources/manifest",
                    "internal-model/build/libs/internal-model.jar");

            Utils.assertArtifactsCreated(buildDir,
                    "service/build/smithyprojections/service/external/build-info/smithy-build-info.json",
                    "service/build/smithyprojections/service/external/sources/model.json",
                    "service/build/smithyprojections/service/external/sources/manifest",
                    "service/build/libs/service.jar");

            // Check that the service jar contains expected files
            Utils.assertJarContains(buildDir, "service/build/libs/service.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/model.json");

            // Get smithy model stored in constructed jar file
            File jarFile = new File(buildDir, "service/build/libs/service.jar");
            Model model = Model.assembler()
                    .addImport(jarFile.getPath())
                    .putProperty(ModelAssembler.ALLOW_UNKNOWN_TRAITS, true)
                    .assemble().unwrap();

            // Check that the model does have at least the basic shapes we expect
            assertTrue(model.getShape(ShapeId.from("smithy.example#Foo")).isPresent());
            assertTrue(model.getShape(ShapeId.from("smithy.example#Baz")).isPresent());
            
            // Validate that model does **not** contain build dep shapes
            assertFalse(model.getShape(ShapeId.from("smithy.example.internal#InternalStructure")).isPresent());
            assertFalse(model.getShape(ShapeId.from("aws.auth#unsignedPayload")).isPresent());
        });
    }
}
