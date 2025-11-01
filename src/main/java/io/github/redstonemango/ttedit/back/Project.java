package io.github.redstonemango.ttedit.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class Project {

    @JsonIgnore private final File dir;
    private String name;
    private int productID;
    private @Nullable String comment;
    private @Nullable String language;

    public Project(File dir, String name, int productID, @Nullable String comment, @Nullable String language) {
        this.dir = dir;
        this.name = name;
        this.productID = productID;
        this.comment = comment;
        this.language = language;
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
}
