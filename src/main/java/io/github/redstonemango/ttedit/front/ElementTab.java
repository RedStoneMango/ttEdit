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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ElementTab extends Tab {

    private final ProjectElement element;

    private IElementEditable editable;

    public ElementTab(ProjectElement element) {
        this.element = element;

        ImageView typeImageView = new ImageView(element.getType().buildImage(element.isChanged()));
        typeImageView.setPreserveRatio(true);
        typeImageView.setFitHeight(20);
        element.changedProperty().addListener((_, _, changed) -> {
            typeImageView.setImage(element.getType().buildImage(changed));
        });

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

        setOnCloseRequest(e -> {
            if (!element.isChanged()) return;

            ButtonType discardButton = new ButtonType("Discard and Close", ButtonBar.ButtonData.RIGHT);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.LEFT);
            ButtonType saveButton = new ButtonType("Save and Close", ButtonBar.ButtonData.RIGHT);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getButtonTypes().setAll(discardButton, cancelButton, saveButton);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("There are unsaved changes in '" + element.getName() + "'");
            alert.setContentText("Do you really want to discard them?");
            UXUtilities.applyStylesheet(alert);
            alert.showAndWait();

            if (alert.getResult() == cancelButton)
                e.consume();
            else if (alert.getResult() == saveButton) {
                save();
            }
        });
    }

    private Node scriptElementContent() {
        ScriptElementEditor editor = new ScriptElementEditor(element);
        editable = editor;
        return editor;
    }

    private Node pageElementContent() {
        Label label = new Label("This is not yet implemented");
        label.setTextFill(Color.RED);
        return label;
    }

    public void save() {
        save(() -> {});
    }

    public void save(Runnable onError) {
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
        element.setChanged(false);
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
