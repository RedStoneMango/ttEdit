package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.property.editor.PropertyEditor;

public class CompletableFieldPropertyEditor implements PropertyEditor<String> {

    private final TextField field;

    public CompletableFieldPropertyEditor(PropertySheet.Item item, String[] completions) {
        field = new TextField(String.valueOf(item.getValue()));
        field.textFormatterProperty().addListener((_, _, newV) ->
            item.setValue(newV)
        );
        TextFields.bindAutoCompletion(field, completions);
    }

    @Override public Node getEditor() { return field; }
    @Override public String getValue() { return field.getText(); }
    @Override public void setValue(String value) { field.setText(value); }
}
