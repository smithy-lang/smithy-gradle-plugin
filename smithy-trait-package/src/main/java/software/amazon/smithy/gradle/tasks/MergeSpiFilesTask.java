/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;


/**
 * Merges two Service Provider files into a new provider file.
 *
 * <p>The generated, merged provider file is written to {@code build/generated-resources/} by default.
 *
 * @see <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Java Service Provider Interface Introduction</a>
 */
public abstract class MergeSpiFilesTask extends DefaultTask {
    private static final String DESCRIPTION = "Merges two Java Service Provider Files.";

    @Inject
    public MergeSpiFilesTask(ProjectLayout projectLayout) {
        getMetaInfDir().convention(projectLayout.getBuildDirectory().dir("generated-resources/META-INF/services"));
        setDescription(DESCRIPTION);
    }

    /**
     * Generated Service Provider file to merge with existing file.
     */
    @InputFile
    public abstract RegularFileProperty getGeneratedFile();

    /**
     * Existing Service Provider file to merge with generated file.
     */
    @InputFile
    public abstract RegularFileProperty getExistingFile();

    /**
     * Output Directory of the task.
     *
     * <p>Note: Marked as internal so that it is not checked for caching, although it can be used as
     * an input property.
     */
    @Internal
    public abstract DirectoryProperty getMetaInfDir();

    /**
     * Read-only property
     *
     * <p>Provides the service provider file name to use based on the provided input files.
     */
    @Internal
    Provider<String> getServiceFileName() {
        return getGeneratedFile().getAsFile().map(File::getName);
    }

    /**
     * Read-only property
     *
     * <p>Gets the output service provider file as a {@link File}.
     */
    @Internal
    Provider<File> getServiceProviderFile() {
        return getMetaInfDir().file(getServiceFileName().get()).map(RegularFile::getAsFile);
    }

    @OutputDirectory
    public Provider<File> getOutputDir() {
        return getMetaInfDir().map(file -> file.getAsFile().getParentFile().getParentFile());
    }

    @TaskAction
    public void mergeFiles() {
        getLogger().info("Merging Service provider files...");
        validate();
        createRequiredFiles();

        try (BufferedReader generated = Files.newBufferedReader(getGeneratedFile().getAsFile().get().toPath());
             BufferedReader existing = Files.newBufferedReader(getExistingFile().getAsFile().get().toPath());
             BufferedWriter output = Files.newBufferedWriter(getServiceProviderFile().get().toPath())
        ) {
            appendInputToOutput(generated, output);
            appendInputToOutput(existing, output);
        } catch (IOException e) {
            throw new GradleException("Failed to merge service provider files.", e);
        }
    }

    private void validate() {
        File generated = getGeneratedFile().getAsFile().get();
        if (!generated.exists()) {
            throw new GradleException("Input file does not exist.");
        }
        File existing = getExistingFile().getAsFile().get();
        if (!existing.exists()) {
            throw new GradleException("Input file does not exist.");
        }
        if (!generated.getName().equals(existing.getName())) {
            throw new GradleException("Could not merge SPI files with differing names");
        }
    }

    private void createRequiredFiles() {
        try {
            Files.createDirectories(getMetaInfDir().get().getAsFile().toPath());
            Files.createFile(getServiceProviderFile().get().toPath());
        } catch (IOException e) {
            throw new GradleException("Could not create service provider file: " + getServiceProviderFile().get(), e);
        }
    }

    private void appendInputToOutput(BufferedReader source, BufferedWriter sink) throws IOException {
        String str;
        while ((str = source.readLine()) != null) {
            sink.write(str + System.lineSeparator());
        }
    }
}
