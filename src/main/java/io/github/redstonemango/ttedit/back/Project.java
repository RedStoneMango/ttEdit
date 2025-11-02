package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.redstonemango.ttedit.Launcher;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private static Project currentProject;

    @JsonIgnore private File dir;
    private int productID;
    private @Nullable String comment;
    private @Nullable String language;

    public Project() {}

    public Project(File dir, int productID, @Nullable String comment, @Nullable String language) {
        this.dir = dir;
        this.productID = productID;
        this.comment = comment;
        this.language = language;
    }

    public void initializeFields(String filename) {
        dir = new File(Launcher.PROJECTS_HOME, filename);
        productID = Math.clamp(productID, 0, 999);
    }

    public File getDir() {
        return dir;
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
