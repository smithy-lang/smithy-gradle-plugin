package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;

public class ProjectsWithTagsTest {
    @Test
    public void testProjectionWithSourceTags() {
        Utils.withCopy("jar-plugin/projects-with-tags", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertJarContains(buildDir,
                                    "build/libs/projects-with-tags.jar",
                                    "META-INF/smithy/manifest",
                                    "META-INF/smithy/model.json");

            Model model = Model.assembler()
                    .addImport(buildDir.toPath().resolve("build").resolve("libs").resolve("projects-with-tags.jar"))
                    .assemble()
                    .unwrap();

            Assertions.assertTrue(model.getShape(ShapeId.from("foo.baz#Integer")).isPresent());
            Assertions.assertTrue(model.getShape(ShapeId.from("foo.baz#Float")).isPresent());
            Assertions.assertTrue(model.getShape(ShapeId.from("smithy.example#Baz")).isPresent());
        });
    }
}
