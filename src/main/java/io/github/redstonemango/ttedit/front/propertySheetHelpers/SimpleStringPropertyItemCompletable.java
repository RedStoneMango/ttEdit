package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import javafx.beans.property.Property;

public class SimpleStringPropertyItemCompletable extends SimplePropertyItem {
    private final String[] completions;

    public SimpleStringPropertyItemCompletable(String name, String category, String description, Property<String> property,
                                               String... completions) {
        super(name, category, description, property);
        this.completions = completions;
    }

    public String[] getCompletions() { return completions; }
}