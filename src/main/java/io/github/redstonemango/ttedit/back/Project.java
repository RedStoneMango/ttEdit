package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.redstonemango.ttedit.Launcher;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class Project {

    private static Project currentProject;

    @JsonIgnore private File dir;
    private String name;
    private int productID;
    private @Nullable String comment;
    private @Nullable String language;

    public Project() {}

    public Project(File dir, String name, int productID, @Nullable String comment, @Nullable String language) {
        this.dir = dir;
        this.name = name;
        this.productID = productID;
        this.comment = comment;
        this.language = language;
    }

    public void ensureFields() {
        if (dir == null) {
            dir = Launcher.PROJECTS_HOME; // Doesn't solve problem but at least we get a "General Config not found" instead of NullPointer
        }
        if (name == null || name.isBlank()) {
            if (dir == Launcher.PROJECTS_HOME) name = "UNNAMED PROJECT";
            else name = dir.getName();
        }
        productID = Math.clamp(productID, 0, 999);
    }

    public File getDir() {
        return dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
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

    public static Project getCurrentProject() {
        return currentProject;
    }
    public static void defineAsCurrentProject(Project project) {
        currentProject = project;
    }
}
