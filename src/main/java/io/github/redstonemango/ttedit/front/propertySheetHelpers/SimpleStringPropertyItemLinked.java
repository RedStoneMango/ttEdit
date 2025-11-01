package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import javafx.beans.property.Property;
import javafx.util.Callback;

public class SimpleStringPropertyItemLinked extends SimplePropertyItem {
    private final Property<String> source;
    private final Callback<String, String> conversionCallback;

    public SimpleStringPropertyItemLinked(String name, String category, String description, Property<String> property,
                                          Property<String> source) {
        this(name, category, description, property, source, s -> s);
    }

    public SimpleStringPropertyItemLinked(String name, String category, String description, Property<String> property,
                                          Property<String> source, Callback<String, String> conversionCallback) {
        super(name, category, description, property);
        this.source = source;
        this.conversionCallback = conversionCallback;
    }

    public Property<String> getSource() {
        return source;
    }

    public Callback<String, String> getConversionCallback() {
        return conversionCallback;
    }
}