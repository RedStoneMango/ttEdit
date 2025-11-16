package io.github.redstonemango.ttedit.front.scriptEditor;

import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScriptActionElement extends AbstractScriptElement{

    public AbstractScriptActionElement(boolean preview, Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                       @Nullable AbstractScriptElement parent, boolean isHead,
                                       ObservableList<ScriptEditor.Branch> branches) {
        super(preview, editorPane, editorScroll, deleteIcon, parent, isHead, branches);
    }
}
