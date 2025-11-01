package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.concurrent.atomic.AtomicBoolean;

public class LinkedFieldPropertyEditor implements PropertyEditor<String> {

    private final TextField field;

    public LinkedFieldPropertyEditor(PropertySheet.Item item, Property<String> source) {
        this(item, source, s -> s);
    }

    public LinkedFieldPropertyEditor(PropertySheet.Item item, Property<String> source, Callback<String, String> conversionCallback) {
        AtomicBoolean independent = new AtomicBoolean(false);
        AtomicBoolean ignoreChange = new AtomicBoolean(false);

        field = new TextField(String.valueOf(item.getValue()));
        field.textProperty().addListener((_, _, newV) -> {
            item.setValue(newV);

            if (ignoreChange.get()) {
                ignoreChange.set(false);
                return;
            }
            independent.set(true);
        });
        source.addListener((_, _, newV) -> {
            if (!independent.get()) {
                ignoreChange.set(true);
                setValue(conversionCallback.call(newV));
            }
        });
    }

    @Override public Node getEditor() { return field; }
    @Override public String getValue() { return field.getText(); }
    @Override public void setValue(String value) { field.setText(value); }
}
