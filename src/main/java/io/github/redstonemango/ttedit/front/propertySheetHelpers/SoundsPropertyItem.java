package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.Sound;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;

public class SoundsPropertyItem extends SimplePropertyItem {

    private final ObservableList<Sound> existingSounds;
    private final Project project;

    public SoundsPropertyItem(String name, String category, String description, ListProperty<Sound> property,
                              ObservableList<Sound> existingSounds, Project project) {
        super(name, category, description, property);
        this.existingSounds = existingSounds;
        this.project = project;
    }
    public SoundsPropertyItem(String name, String category, String description, ListProperty<Sound> property,
                              ObservableList<Sound> existingSounds, Project project, boolean disabled) {
        super(name, category, description, property, disabled);
        this.existingSounds = existingSounds;
        this.project = project;
    }

    public ObservableList<Sound> getExistingSounds() {
        return existingSounds;
    }

    public Project getProject() {
        return project;
    }
}