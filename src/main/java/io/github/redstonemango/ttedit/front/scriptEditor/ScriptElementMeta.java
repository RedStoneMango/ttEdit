package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.front.ElementTab;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.Set;

public record ScriptElementMeta(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                ObservableList<ScriptElementEditor.Branch> branches, ProjectElement element,
                                ObservableList<ProjectElement> existingScripts) {
}
