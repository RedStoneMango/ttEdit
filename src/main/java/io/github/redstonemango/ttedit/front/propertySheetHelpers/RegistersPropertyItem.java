package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import javafx.beans.property.MapProperty;

public class RegistersPropertyItem extends SimplePropertyItem {
    private final Project project;
    public RegistersPropertyItem(String name, String category, String description, MapProperty<String, Integer> registers,
                                 Project project) {
        super(name, category, description, registers);
        this.project = project;
    }
    public RegistersPropertyItem(String name, String category, String description, MapProperty<String, Integer> registers,
                                 Project project, boolean disabled) {
        super(name, category, description, registers, disabled);
        this.project = project;
    }

    public Project getProject() {
        return project;
    }
}