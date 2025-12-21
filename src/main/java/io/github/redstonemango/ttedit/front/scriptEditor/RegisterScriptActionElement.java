package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.beans.property.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

public class RegisterScriptActionElement extends AbstractScriptActionElement {

    public RegisterScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent,
                                       ScriptElementMeta meta) {
        super(preview, parent, false, meta);
    }

    public static RegisterScriptActionElement createPreview(ScriptElementMeta meta) {
        return new RegisterScriptActionElement(true, null, meta);
    }

    private StringProperty register;
    private Property<ScriptData.Action> action;
    private StringProperty value;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (register == null) register = new SimpleStringProperty("");
        if (action == null) action = new SimpleObjectProperty<>(ScriptData.Action.SET);
        if (value == null) value = new SimpleStringProperty("");

        Label l = new Label("Set");
        applyColoring(l);
        l.setMouseTransparent(preview);

        TextField registerField = new TextField("");
        registerField.setPrefWidth(70);
        registerField.setMouseTransparent(preview);
        registerField.setFocusTraversable(false);
        registerField.textProperty().addListener((_, _, val) -> {
            register.set(val);
            markChanged();
        });
        register.addListener((_, _, val) -> registerField.setText(val));
        UXUtilities.applyRegisterCompletion(registerField, element,
                Project.getCurrentProject().getRegisterIndexUnifier(), false);
        applyColoring(registerField);

        Label l2 = new Label("to");
        applyColoring(l2);
        l2.setMouseTransparent(preview);

        ComboBox<ScriptData.Action> b = new ComboBox<>();
        b.getItems().addAll(ScriptData.Action.values());
        b.setPrefWidth(190);
        b.setMouseTransparent(preview);
        b.setFocusTraversable(false);
        UXUtilities.applyActionComboBoxCellFactory(b);
        b.getSelectionModel().select(action.getValue());
        b.getSelectionModel().selectedItemProperty()
                .addListener((_, _, val) -> {
                    action.setValue(val);
                    markChanged();
                });
        action.addListener((_, _, val) -> b.getSelectionModel().select(val));
        applyColoring(b);

        TextField valueField = new TextField("");
        valueField.setPrefWidth(70);
        valueField.setMouseTransparent(preview);
        valueField.setFocusTraversable(false);
        valueField.textProperty().addListener((_, _, val) -> {
            value.set(val);
            markChanged();
        });
        value.addListener((_, _, val) -> valueField.setText(val));
        UXUtilities.applyRegisterCompletion(valueField, element,
                Project.getCurrentProject().getRegisterIndexUnifier(), true);
        applyColoring(valueField);

        contentBox.getChildren().addAll(l, registerField, l2, b, valueField);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.REGISTER) throw new IllegalArgumentException("ScriptData has to be of type REGISTER");
        register.set(data.getRegister());
        action.setValue(data.getAction());
        value.set(data.getValue());
        markIsInBranch();
    }

    @Override
    public AbstractScriptElement createDefault(ScriptElementMeta meta) {
        return new RegisterScriptActionElement(false, null, meta);
    }

    @Override
    public Color color() {
        return Color.HOTPINK;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.REGISTER);
        data.setRegister(register.get());
        data.setAction(action.getValue());
        data.setValue(value.get());
        return data;
    }

    @Override
    public double width() {
        return 400;
    }

    @Override
    public double height() {
        return 20;
    }
}
