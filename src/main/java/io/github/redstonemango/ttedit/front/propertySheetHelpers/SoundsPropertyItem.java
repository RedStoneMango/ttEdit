package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.Sound;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;

import java.util.Set;

public class SoundsPropertyItem extends SimplePropertyItem {
    private final Project project;
    public SoundsPropertyItem(String name, String category, String description, ListProperty<Sound> sounds,
                              Project project) {
        super(name, category, description, sounds);
        this.project = project;
    }
    public SoundsPropertyItem(String name, String category, String description, ListProperty<Sound> sounds,
                              Project project, boolean disabled) {
        super(name, category, description, sounds, disabled);
        this.project = project;
    }

    public Project getProject() {
        return project;
    }
}