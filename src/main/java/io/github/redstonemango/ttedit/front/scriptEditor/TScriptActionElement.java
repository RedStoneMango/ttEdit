package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

public class TScriptActionElement extends AbstractScriptActionElement {

    public TScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent,
                                ScriptElementMeta meta) {
        super(preview, parent, false, meta);
    }

    public static TScriptActionElement createPreview(ScriptElementMeta meta) {
        return new TScriptActionElement(true, null,meta);
    }

    private StringProperty register;
    private StringProperty modulo;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (register == null) register = new SimpleStringProperty("");
        if (modulo == null) modulo = new SimpleStringProperty("");

        Label l = new Label("Store internal timestamp in");
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
        UXUtilities.applyRegisterCompletion(registerField, element, Project.getCurrentProject().getRegisterIndexUnifier());
        applyColoring(registerField);

        Label l2 = new Label("with max value");
        applyColoring(l2);
        l2.setMouseTransparent(preview);

        TextField moduloField = new TextField("");
        moduloField.setPrefWidth(70);
        moduloField.setMouseTransparent(preview);
        moduloField.setFocusTraversable(false);
        moduloField.textProperty().addListener((_, _, val) -> {
            modulo.set(val);
            markChanged();
        });
        modulo.addListener((_, _, val) -> moduloField.setText(val));
        UXUtilities.applyRegisterCompletion(moduloField, element, Project.getCurrentProject().getRegisterIndexUnifier());
        applyColoring(moduloField);

        contentBox.getChildren().addAll(l, registerField, l2, moduloField);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.TIME) throw new IllegalArgumentException("ScriptData has to be of type TIME");
        register.set(data.getRegister());
        modulo.set(data.getModulo());
        markIsInBranch();
    }

    @Override
    public AbstractScriptElement createDefault(ScriptElementMeta meta) {
        return new TScriptActionElement(false, null, meta);
    }

    @Override
    public Color color() {
        return Color.INDIANRED;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.TIME);
        data.setRegister(register.get());
        data.setModulo(modulo.get());
        return data;
    }

    @Override
    public double width() {
        return 430;
    }

    @Override
    public double height() {
        return 20;
    }
}
