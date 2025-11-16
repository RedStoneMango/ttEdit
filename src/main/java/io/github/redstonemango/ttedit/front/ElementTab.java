package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.scriptEditor.ScriptElementEditor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;

public class ElementTab extends Tab {

    private final ProjectElement element;

    private IElementEditable editable;

    public ElementTab(ProjectElement element) {
        this.element = element;

        ImageView typeImageView = new ImageView(element.getType().buildImage());
        typeImageView.setPreserveRatio(true);
        typeImageView.setFitHeight(20);

        setText(element.getName());
        setGraphic(typeImageView);

        Node editor = element.getType() == ProjectElement.Type.SCRIPT
                        ? scriptElementContent()
                        : pageElementContent();
        VBox.setVgrow(editor, Priority.ALWAYS);
        Platform.runLater(editor::requestFocus);

        Button saveAndCloseButton = new Button("Save and Close");
        saveAndCloseButton.setOnAction(_ -> {
            save();
            getTabPane().getTabs().remove(this);
        });
        saveAndCloseButton.setPrefWidth(150);
        saveAndCloseButton.setStyle("-fx-base: green;");
        Button saveWithoutCloseButton = new Button("Save");
        saveWithoutCloseButton.setOnAction(_ -> save());
        saveWithoutCloseButton.setPrefWidth(150);

        HBox buttonBox = new HBox(5, saveWithoutCloseButton, saveAndCloseButton);
        VBox.setMargin(buttonBox, new Insets(5));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(buttonBox, editor);
        setContent(content);
    }

    private Node scriptElementContent() {
        ScriptElementEditor editor = new ScriptElementEditor();
        editable = editor;
        return editor;
    }

    private Node pageElementContent() {
        Label label = new Label("This is not yet implemented");
        label.setTextFill(Color.RED);
        return label;
    }

    private void save() {
        if (editable instanceof ScriptElementEditor editor) {
            List<ScriptData> elementBranches = element.getBranches();
            assert elementBranches != null: "Elements that have a ScriptElementEditor should always supply branches";
            elementBranches.clear();
            editor.getBranches().forEach(editorBranch -> elementBranches.add(editorBranch.head().build()));
            try {
                ProjectIO.saveProjectElement(element, Project.getCurrentProject());
            } catch (IOException e) {
                UXUtilities.errorAlert("Error saving element '" + element.getName() + "'", e.getMessage());
            }
        }
    }

    public ProjectElement getElement() {
        return element;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof ElementTab tab)) return false;
        return tab.element == element;
    }
}
