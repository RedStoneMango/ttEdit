package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.redstonemango.mangoutils.MangoIO;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ProjectLoadException;
import io.github.redstonemango.ttedit.back.registerDictionary.RegisterFileIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ProjectIO {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
    static {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    public static void saveProjectGeneralConfig(Project project) throws IOException {
        File generalConfigFile = new File(project.getDir(), "general.json");
        if (!generalConfigFile.exists()) {
            generalConfigFile.getParentFile().mkdirs();
            generalConfigFile.createNewFile();
        }

        objectMapper.writer(prettyPrinter)
                .writeValue(generalConfigFile, project);
    }

    public static void saveProjectElement(ProjectElement element, Project owner) throws IOException {
        if (!owner.getElementDir().exists()) {
            owner.getElementDir().mkdir();
        }

        File file = new File(owner.getElementDir(),
                element.getName() + element.getType().fileSuffix());

        objectMapper.writer(prettyPrinter)
                .writeValue(file, element);
        element.setChanged(false);
    }

    public static void deleteProjectElement(ProjectElement element, Project owner) throws IOException {
        if (!owner.getElementDir().exists()) {
            return;
        }

        File file = new File(owner.getElementDir(),
                element.getName() + element.getType().fileSuffix());

        if (file.exists()) {
            Files.delete(file.toPath()); // Better error handling using NIO
        }
    }

    public static void deleteProject(File projectFile) throws IOException {
        MangoIO.deleteDirectoryRecursively(projectFile);
    }

    public static void deleteProject(Project project) throws IOException {
        deleteProject(project.getDir());
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
                    project.getElements().add(
                            loadProjectElement(elementFile, project.getRegisterIndexUnifier().getFileIndex())
                    );
                } catch (IOException | ProjectLoadException e) {
                    onException.accept(e);
                }
            }
        }
    }

    public static ProjectElement loadProjectElement(File elementFile, RegisterFileIndex registerFileIndex)
            throws IOException, ProjectLoadException {

        ProjectElement element = objectMapper.readValue(elementFile, ProjectElement.class);
        element.initializeFields(elementFile.getName());
        registerFileIndex.loadEntries(element);
        return element;
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
        project.getRegisterIndexUnifier().update(); // Load registers from script elements
        return project;
    }
}
