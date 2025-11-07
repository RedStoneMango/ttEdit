package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redstonemango.mangoutils.MangoIO;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ProjectIO {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static void saveProjectGeneralConfig(Project project) throws IOException {
        File generalConfigFile = new File(project.getDir(), "general.json");
        if (!generalConfigFile.exists()) {
            generalConfigFile.getParentFile().mkdir(); // No need for complex mkdirs: All parent dirs already exist
            generalConfigFile.createNewFile();
        }

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(generalConfigFile, project);
    }

    public static void saveProjectElement(ProjectElement element, Project owner) throws IOException {
        if (!owner.getElementDir().exists()) {
            owner.getElementDir().mkdir();
        }

        File file = new File(owner.getElementDir(),
                element.getName() + "." + element.getType().toString().toLowerCase() + ".json");

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, element);
    }

    public static void deleteProjectElement(ProjectElement element, Project owner) throws IOException {
        if (!owner.getElementDir().exists()) {
            return;
        }

        File file = new File(owner.getElementDir(),
                element.getName() + "." + element.getType().toString().toLowerCase() + ".json");

        if (file.exists()) {
            Files.delete(file.toPath()); // Better error handling using NIO
        }
    }

    public static void deleteProject(Project project) throws IOException {
        MangoIO.deleteDirectoryRecursively(project.getDir());
    }

    public static void saveProject(Project project, Consumer<Exception> onException) {
        try {
            saveProjectGeneralConfig(project);
        } catch (IOException e) {
            onException.accept(e);
        }

        project.getElements().forEach(element -> {
            try {
                saveProjectElement(element, project);
            } catch (IOException e) {
                onException.accept(e);
            }
        });
    }

    private static Project loadProjectFromGeneralConfig(File projectDir) throws IOException {
        File generalConfigFile = new File(projectDir, "general.json");
        if (generalConfigFile.exists()) {
            return objectMapper.readValue(generalConfigFile, Project.class);
        }
        throw new IOException("Expected file '" + generalConfigFile.getPath() + "' does not exist");
    }

    private static void loadProjectElements(Project project, Consumer<Exception> onException) {
        String[] elementsNames = project.getElementDir().list((dir, name) ->
                dir.equals(project.getElementDir()) && (name.endsWith(".script.json") || name.endsWith(".page.json")));

        if (elementsNames != null) {
            for (String elementName : elementsNames) {
                File elementFile = new File(project.getElementDir(), elementName);
                try {
                    ProjectElement element = objectMapper.readValue(elementFile, ProjectElement.class);
                    element.initializeFields(elementName);
                    project.getElements().add(element);
                } catch (IOException e) {
                    onException.accept(e);
                }
            }
        }
    }

    public static Project loadProject(File projectDir, Consumer<Exception> onException) {
        Project project;
        try {
            project = loadProjectFromGeneralConfig(projectDir);
            project.initializeFields(projectDir.getName());
        } catch (IOException e) {
            onException.accept(e);
            return null;
        }

        loadProjectElements(project, onException);
        return project;
    }
}
