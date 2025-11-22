package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

public class RegisterScriptActionElement extends AbstractScriptActionElement {

    public RegisterScriptActionElement(boolean preview, Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                       @Nullable AbstractScriptElement parent, BooleanProperty changed,
                                       ObservableList<ScriptElementEditor.Branch> branches) {
        super(preview, editorPane, editorScroll, deleteIcon, parent, false, changed, branches);
    }

    public static RegisterScriptActionElement createPreview(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                                            BooleanProperty changed,
                                                            ObservableList<ScriptElementEditor.Branch> branches) {
        return new RegisterScriptActionElement(true, editorPane, editorScroll, deleteIcon, null, changed, branches);
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
        applyColoring(registerField);

        Label l2 = new Label("to");
        applyColoring(l2);
        l2.setMouseTransparent(preview);

        ComboBox<ScriptData.Action> b = new ComboBox<>();
        b.getItems().addAll(ScriptData.Action.values());
        b.setPrefWidth(150);
        b.setMouseTransparent(preview);
        b.setFocusTraversable(false);
        UXUtilities.applyActionComboBoxCellFactory(b);
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
    public AbstractScriptElement createDefault(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                               @Nullable AbstractScriptElement parent) {
        return new RegisterScriptActionElement(false, editorPane, editorScroll, deleteIcon, parent, changed, branches);
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
        return 360;
    }

    @Override
    public double height() {
        return 20;
    }
}
