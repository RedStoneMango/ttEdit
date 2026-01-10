package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.redstonemango.ttedit.Launcher;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.registerDictionary.RegisterIndexUnifier;
import io.github.redstonemango.ttedit.front.scriptEditor.ScriptElementEditor;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private static Project currentProject;

    @JsonIgnore private File dir;
    @JsonIgnore private ObservableList<ProjectElement> elements;
    @JsonIgnore private ObservableList<ProjectElement> scripts;
    @JsonIgnore private ObservableList<Sound> sounds;
    @JsonIgnore private RegisterIndexUnifier registerIndexUnifier;

    private int productID;
    private List<String> welcomeSounds;
    private @Nullable String comment;
    private @Nullable String language;
    private double scriptBoxLibraryWidth = ScriptElementEditor.MAX_LIBRARY_WIDTH;
    private final Map<String, Integer> initialRegisters = new HashMap<>();

    public Project() {}

    public Project(File dir, int productID, @Nullable String comment, @Nullable String language) {
        this.dir = dir;
        this.productID = productID;
        this.comment = comment;
        this.language = language;
    }

    public void initializeFields(String filename)  {
        productID = Math.clamp(productID, 0, 999);
        scriptBoxLibraryWidth = Math.clamp(
                scriptBoxLibraryWidth,
                ScriptElementEditor.MIN_LIBRARY_WIDTH,
                ScriptElementEditor.MAX_LIBRARY_WIDTH
        );
        if (welcomeSounds == null) welcomeSounds = new ArrayList<>();

        // Non-persistent data
        dir = new File(Launcher.PROJECTS_HOME, filename);
        sounds = FXCollections.observableArrayList();
        registerIndexUnifier = RegisterIndexUnifier.create(this);
        scripts = FXCollections.observableArrayList();
        elements = FXCollections.observableArrayList();
        elements.addListener((ListChangeListener<? super ProjectElement>) l -> {
            while (l.next()) {
                if (l.wasAdded()) {
                    l.getAddedSubList().stream()
                            .filter(e ->  e.getType() == ProjectElement.Type.SCRIPT)
                            .forEach(e -> scripts.add(e));
                }
                if (l.wasRemoved()) {
                    l.getRemoved().stream()
                            .filter(e ->  e.getType() == ProjectElement.Type.SCRIPT)
                            .forEach(e -> scripts.remove(e));
                }
            }
        });
    }

    public File getDir() {
        return dir;
    }

    public File getElementDir() {
        return new File(dir, "content");
    }

    public File getSoundDir() {
        return new File(dir, "sounds");
    }

    public String name() {
        return dir.getName();
    }

    public List<String> getWelcomeSounds() {
        return welcomeSounds;
    }

    public void setWelcomeSounds(List<String> welcomeSounds) {
        this.welcomeSounds = welcomeSounds;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public double getScriptBoxLibraryWidth() {
        return scriptBoxLibraryWidth;
    }

    public void setScriptBoxLibraryWidth(double scriptBoxLibraryWidth) {
        this.scriptBoxLibraryWidth = scriptBoxLibraryWidth;
    }

    public @Nullable String getComment() {
        return comment == null ? null : (comment.isBlank() ? null : comment);
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    public @Nullable String getLanguage() {
        return language == null ? null : (language.isBlank() ? null : language);
    }

    public void setLanguage(@Nullable String language) {
        this.language = language;
    }

    public ObservableList<ProjectElement> getElements() {
        return elements;
    }


    public ObservableList<Sound> getSounds() {
        return sounds;
    }

    public ObservableList<ProjectElement> getScripts() {
        return scripts;
    }

    public RegisterIndexUnifier getRegisterIndexUnifier() {
        return registerIndexUnifier;
    }

    public static Project getCurrentProject() {
        return currentProject;
    }
    public static void defineAsCurrentProject(Project project) {
        currentProject = project;
    }

    public Map<String, Integer> getInitialRegisters() {
        return initialRegisters;
    }
}
