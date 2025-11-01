package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

public class SimplePropertyItem implements PropertySheet.Item {

    private final String name;
    private final String category;
    private final String description;
    private final Property<?> property;
    private final boolean disabled;

    public SimplePropertyItem(String name, String category, String description, Property<?> property) {
        this(name, category, description, property, false);
    }

    public SimplePropertyItem(String name, String category, String description, Property<?> property, boolean disabled) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.property = property;
        this.disabled = disabled;
    }

    @Override public Class<?> getType() {
        Object val = property.getValue();
        return (val != null) ? val.getClass() : Object.class;
    }

    @Override public String getCategory() { return category; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public Object getValue() { return property.getValue(); }
    @Override public Optional<ObservableValue<?>> getObservableValue() { return Optional.of(property); }
    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        ((Property<Object>) property).setValue(value);
    }

    public boolean isDisabled() { return disabled; }
}
