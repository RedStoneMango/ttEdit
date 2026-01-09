package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public record ScriptElementMeta(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                ObservableList<ScriptElementEditor.Branch> branches, ProjectElement element,
                                Project project) {
}
