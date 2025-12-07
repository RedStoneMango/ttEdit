package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.redstonemango.ttedit.Launcher;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.registerDictionary.RegisterIndexUnifier;
import io.github.redstonemango.ttedit.front.scriptEditor.ScriptElementEditor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private static Project currentProject;

    @JsonIgnore private File dir;
    @JsonIgnore private Set<ProjectElement> elements;
    @JsonIgnore private RegisterIndexUnifier registerIndexUnifier;

    private int productID;
    private @Nullable String comment;
    private @Nullable String language;
    private double scriptBoxLibraryWidth = ScriptElementEditor.MAX_LIBRARY_WIDTH;

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

        // Non-persisting data
        dir = new File(Launcher.PROJECTS_HOME, filename);
        elements = new HashSet<>();
        registerIndexUnifier = RegisterIndexUnifier.create(this);
    }

    public File getDir() {
        return dir;
    }

    public File getElementDir() {
        return new File(dir, "content");
    }

    public String name() {
        return dir.getName();
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

    public Set<ProjectElement> getElements() {
        return elements;
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
}
