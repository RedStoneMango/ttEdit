package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

public class JScriptActionElement extends AbstractScriptActionElement {

    public JScriptActionElement(boolean preview, Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                @Nullable AbstractScriptElement parent,
                                ObservableList<ScriptElementEditor.Branch> branches) {
        super(preview, editorPane, editorScroll, deleteIcon, parent, false, branches);
    }

    public static JScriptActionElement createPreview(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                                     ObservableList<ScriptElementEditor.Branch> branches) {
        return new JScriptActionElement(true, editorPane, editorScroll, deleteIcon, null, branches);
    }

    private StringProperty jumpTarget;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (jumpTarget == null) jumpTarget = new SimpleStringProperty("");

        Label l = new Label("Jump To");
        applyColoring(l);
        l.setMouseTransparent(preview);
        ComboBox<String> b = new ComboBox<>(FXCollections.observableArrayList("hello", "test", "hi"));
        b.setPrefWidth(200);
        b.setMouseTransparent(preview);
        b.setFocusTraversable(false);
        b.getSelectionModel().selectedItemProperty()
                .addListener((_, _, val) -> jumpTarget.set(val));
        jumpTarget.addListener((_, _, val) ->
                b.getSelectionModel().select(val));
        applyColoring(b);
        contentBox.getChildren().addAll(l, b);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.JUMP) throw new IllegalArgumentException("ScriptData has to be of type JUMP");
        jumpTarget.set(data.getJumpTarget());
    }

    @Override
    public AbstractScriptElement createDefault(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                               @Nullable AbstractScriptElement parent,
                                               ObservableList<ScriptElementEditor.Branch> branches) {
        return new JScriptActionElement(false, editorPane, editorScroll, deleteIcon, parent, branches);
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
        if (hasElementChild()) {
            data.setChild(getElementChild().build());
        }
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
