package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class RegistersPropertyEditor implements PropertyEditor<Map<String, Integer>> {

    private static final Pattern POSITIVE_INT_PATTERN = Pattern.compile("^\\d{0,5}$");

    private final Button button;
    private Map<String, Integer> registers;
    private final PopOver popOver;
    private final Project project;

    @SuppressWarnings("unchecked")
    public RegistersPropertyEditor(RegistersPropertyItem item) {
        project = item.getProject();
        registers = (Map<String, Integer>) item.getValue();

        popOver = new PopOver();

        button = new Button(
                project != null
                        ? buildInitialRegistersText()
                        : ""
        );
        button.setOnAction(_ -> {
            popOver.setContentNode(createPopOverContent());
            popOver.show(button);
        });
        button.setAlignment(Pos.CENTER_LEFT);

        popOver.setDetachable(false);
        popOver.setAnimated(true);
        popOver.setTitle("Initial Registers");
        popOver.setOnHidden(_ -> button.setText(buildInitialRegistersText()));
    }

    private Node createPopOverContent() {
        VBox elements = new VBox(10);

        ScrollPane scroll = new ScrollPane(elements);
        scroll.setPrefWidth(300);
        scroll.setMinHeight(50);
        scroll.setMaxHeight(250);
        scroll.setPadding(new Insets(5));
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.vvalueProperty().bind(elements.heightProperty());

        Button addBtn = new Button("+ Register");
        addBtn.setPrefWidth(290);
        elements.getChildren().add(addBtn);

        addBtn.setOnAction(_ ->
            addRegister(elements, "", 0, true)
        );
        registers.forEach((name, val) -> addRegister(elements, name, val, false));
        popOver.setOnHiding(_ -> addBtn.requestFocus()); // Unfocus current text field to enure that all changes get indexed

        return scroll;
    }

    private void addRegister(VBox parent, String regName, int regValue, boolean focus) {
        HBox box = new HBox(5);
        String[] name = {regName};
        boolean[] exists = {true};
        TextField registerField = new TextField(name[0]);
        TextField valueField = new TextField(String.valueOf(regValue));
        {
            Button removeBtn = new Button("x");
            removeBtn.setOnAction(_ -> {
                parent.getChildren().remove(box);
                registers.remove(name[0]);
                exists[0] = false;
            });

            HBox.setHgrow(registerField, Priority.ALWAYS);
            registerField.focusedProperty().addListener((_, _, focused) -> {
                if (!focused && exists[0]) {
                    int val = registers.getOrDefault(name[0], 0);
                    registers.remove(name[0]);
                    String registerName = ScriptData.forceRegisterPattern(registerField.getText());
                    registerField.setText(registerName);
                    registers.put(registerName, val);
                    name[0] = registerName;
                }
            });
            UXUtilities.applyReadonlyRegisterCompletion(
                    registerField,
                    project.getRegisterIndexUnifier(),
                    () -> registers.keySet()
            );

            valueField.setPrefWidth(60);
            valueField.textProperty().addListener((_, previous, current) -> {
                if (!POSITIVE_INT_PATTERN.matcher(current).matches()) {
                    valueField.setText(previous);
                }
            });
            valueField.focusedProperty().addListener((_, _, isFocused) -> {
                if (!isFocused && exists[0]) {
                    if (valueField.getText().isBlank()) {
                        valueField.setText("0");
                    }
                    int val = (int) Long.parseLong(valueField.getText()); // "String -> long -> int" to correctly handle strings in "65536..99999"
                    val = Math.clamp(val, 0, 65535);
                    valueField.setText(String.valueOf(val));

                    registers.put(name[0], val);
                }
            });

            box.getChildren().addAll(registerField, valueField, removeBtn);
        }

        registers.put(name[0], regValue);
        parent.getChildren().add(parent.getChildren().size() - 1, box);
        if (focus) registerField.requestFocus();
    }

    private String buildInitialRegistersText() {
        if (registers.isEmpty()) return "-- None --";

        StringBuilder builder = new StringBuilder();
        boolean firstCall = true;
        for (String register : registers.keySet()) {
            int value = registers.get(register);

            if (!firstCall) builder.append(", ");
            builder.append(register).append(": ").append(value);

            firstCall = false;
        }
        return builder.toString();
    }

    @Override public Node getEditor() { return button; }
    @Override public Map<String, Integer> getValue() { return registers; }

    @Override
    public void setValue(Map<String, Integer> value) {
        registers = value;
        button.setText(buildInitialRegistersText());
        popOver.setContentNode(createPopOverContent());
    }
}
