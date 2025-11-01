package io.github.redstonemango.ttedit.front;

import javafx.beans.property.Property;

public class SimpleNumberPropertyItem<T extends Number> extends SimplePropertyItem {
    private final double min;
    private final double max;

    public SimpleNumberPropertyItem(String name, String category, String description, Property<T> property, double min, double max) {
        super(name, category, description, property);
        this.min = min;
        this.max = max;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
}