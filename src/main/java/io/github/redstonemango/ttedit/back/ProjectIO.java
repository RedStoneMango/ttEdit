package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ProjectIO {

    public static void saveProjectGeneralConfig(Project project) throws IOException {
        File generalConfigFile = new File(project.getDir(), "general.json");
        if (!generalConfigFile.exists()) {
            generalConfigFile.getParentFile().mkdir(); // No need for complex mkdirs: All parent dirs already exist
            generalConfigFile.createNewFile();
        }

        new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValue(generalConfigFile, project);
    }

    private static Project loadProjectFromGeneralConfig(File projectDir) throws IOException {
        File generalConfigFile = new File(projectDir, "general.json");
        if (generalConfigFile.exists()) {
            return new ObjectMapper().readValue(generalConfigFile, Project.class);
        }
        throw new IOException("Expected file '" + generalConfigFile.getPath() + "' does not exist");
    }

    public static Project loadProject(File projectDir) throws IOException {
        Project project = loadProjectFromGeneralConfig(projectDir);
        project.ensureFields();
        // Later, we will add more method calls here to also load scripts etc
        return project;
    }

}
