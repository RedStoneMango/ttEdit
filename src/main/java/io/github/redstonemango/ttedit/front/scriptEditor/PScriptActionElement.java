package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

public class PScriptActionElement extends AbstractScriptActionElement {

    public PScriptActionElement(boolean preview, Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon, @Nullable AbstractScriptElement parent,
                                ObservableList<ScriptElementEditor.Branch> branches) {
        super(preview, editorPane, editorScroll, deleteIcon, parent, false, branches);
    }

    public static PScriptActionElement createPreview(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                                     ObservableList<ScriptElementEditor.Branch> branches) {
        return new PScriptActionElement(true, editorPane, editorScroll, deleteIcon, null, branches);
    }

    private StringProperty sound;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (sound == null) sound = new SimpleStringProperty("");

        Label l = new Label("Play sound");
        applyColoring(l);
        l.setMouseTransparent(preview);
        TextField f = new TextField("");
        f.setPrefWidth(140);
        f.setMouseTransparent(preview);
        f.setFocusTraversable(false);
        f.textProperty().addListener((_, _, val) -> sound.set(val));
        sound.addListener((_, _, val) -> f.setText(val));
        applyColoring(f);
        contentBox.getChildren().addAll(l, f);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.PLAY) throw new IllegalArgumentException("ScriptData has to be of type PLAY");
        sound.set(data.getSound());
    }

    @Override
    public AbstractScriptElement createDefault(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                               @Nullable AbstractScriptElement parent,
                                               ObservableList<ScriptElementEditor.Branch> branches) {
        return new PScriptActionElement(false, editorPane, editorScroll, deleteIcon, parent, branches);
    }

    @Override
    public Color color() {
        return Color.CYAN;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.PLAY);
        data.setSound(sound.get());
        return data;
    }

    @Override
    public double width() {
        return 220;
    }

    @Override
    public double height() {
        return 20;
    }
}
