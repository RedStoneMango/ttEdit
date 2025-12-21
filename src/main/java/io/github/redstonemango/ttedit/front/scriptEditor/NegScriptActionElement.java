package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

public class NegScriptActionElement extends AbstractScriptActionElement {

    public NegScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent,
                                  ScriptElementMeta meta) {
        super(preview, parent, false, meta);
    }

    public static NegScriptActionElement createPreview(ScriptElementMeta meta) {
        return new NegScriptActionElement(true, null, meta);
    }

    private StringProperty register;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (register == null) register = new SimpleStringProperty("");

        Label l = new Label("Negate");
        applyColoring(l);
        l.setMouseTransparent(preview);

        TextField registerField = new TextField("");
        registerField.setPrefWidth(100);
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

        contentBox.getChildren().addAll(l, registerField);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.NEGATE) throw new IllegalArgumentException("ScriptData has to be of type NEGATE");
        register.set(data.getRegister());
        markIsInBranch();
    }

    @Override
    public AbstractScriptElement createDefault(ScriptElementMeta meta) {
        return new NegScriptActionElement(false, null, meta);
    }

    @Override
    public Color color() {
        return Color.DEEPPINK;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.NEGATE);
        data.setRegister(register.get());
        return data;
    }

    @Override
    public double width() {
        return 160;
    }

    @Override
    public double height() {
        return 20;
    }
}
