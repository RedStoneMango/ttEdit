package io.github.redstonemango.ttedit.front.scriptEditor;

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
                                ObservableList<ScriptEditor.Branch> branches) {
        super(preview, editorPane, editorScroll, deleteIcon, parent, false, branches);
    }

    public static JScriptActionElement createPreview(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                                     ObservableList<ScriptEditor.Branch> branches) {
        return new JScriptActionElement(true, editorPane, editorScroll, deleteIcon, null, branches);
    }

    @Override
    public void populate(HBox contentBox, boolean preview) {
        Label l = new Label("Jump To");
        applyColoring(l);
        l.setMouseTransparent(preview);
        ComboBox<String> b = new ComboBox<>(FXCollections.observableArrayList("hello", "test", "hi"));
        b.setPrefWidth(200);
        b.setMouseTransparent(preview);
        b.setFocusTraversable(false);
        applyColoring(b);
        contentBox.getChildren().addAll(l, b);
    }

    @Override
    public AbstractScriptElement createDefault(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                               @Nullable AbstractScriptElement parent,
                                               ObservableList<ScriptEditor.Branch> branches) {
        return new JScriptActionElement(false, editorPane, editorScroll, deleteIcon, parent, branches);
    }

    @Override
    public Color color() {
        return Color.ORANGE;
    }

    @Override
    public String build() {
        return "";
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
