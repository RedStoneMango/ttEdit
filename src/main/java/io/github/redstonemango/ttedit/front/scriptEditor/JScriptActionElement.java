package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class JScriptActionElement extends AbstractScriptActionElement {

    public JScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent,
                                ScriptElementMeta meta) {
        super(preview, parent, false, meta);
    }

    public static JScriptActionElement createPreview(ScriptElementMeta meta) {
        return new JScriptActionElement(true, null, meta);
    }

    private StringProperty jumpTarget;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (jumpTarget == null) jumpTarget = new SimpleStringProperty(existingScripts.getFirst().getName());

        Label l = new Label("Jump To");
        applyColoring(l);
        l.setMouseTransparent(preview);
        ComboBox<ProjectElement> b = new ComboBox<>(existingScripts);
        b.setPrefWidth(200);
        b.setMouseTransparent(preview);
        b.setFocusTraversable(false);
        b.getSelectionModel().select(existingScripts.getFirst());
        b.getSelectionModel().selectedItemProperty()
                .addListener((_, _, val) -> {
                    if (val != null) jumpTarget.set(val.getName());
                    markChanged();
                });
        jumpTarget.addListener((_, _, val) -> {
                b.getSelectionModel().select(existingScripts.stream()
                        .filter(e -> e.getName().equals(val))
                        .findFirst()
                        .orElse(existingScripts.getFirst()));
        });
        applyColoring(b);
        UXUtilities.applyProjectElementComboBoxCellFactory(b);
        contentBox.getChildren().addAll(l, b);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.JUMP) throw new IllegalArgumentException("ScriptData has to be of type JUMP");
        jumpTarget.set(data.getJumpTarget());
        markIsInBranch();
    }

    @Override
    public AbstractScriptElement createDefault(ScriptElementMeta meta) {
        return new JScriptActionElement(false, null, meta);
    }

    @Override
    public Color color() {
        return Color.ORANGE;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.JUMP);
        data.setJumpTarget(jumpTarget.get());
        return data;
    }

    @Override
    public double width() {
        return 260;
    }

    @Override
    public double height() {
        return 20;
    }
}
