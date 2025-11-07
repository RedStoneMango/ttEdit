package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.ttedit.Launcher;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.SimplePropertyItem;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.SimpleNumberPropertyItem;
import io.github.redstonemango.ttedit.front.UXUtilities;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.SimpleStringPropertyItemCompletable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.PropertySheet;

import java.io.File;
import java.io.IOException;

public class ProjectCreationController {

    @FXML private PropertySheet propertySheet;
    @FXML private Button createButton;

    private SimpleStringProperty projectName;
    private SimpleIntegerProperty productID;
    private SimpleStringProperty comment;
    private SimpleStringProperty language;

    @FXML
    private void initialize() {
        UXUtilities.doOnceSceneLoads(propertySheet, scene ->
            scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    onClose();
            })
        );

        UXUtilities.applyPropertyEditorFactory(propertySheet);
        propertySheet.getItems().addAll(buildConfigList());

        createButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            String name = projectName.getValue();
            name = asFriendlyText(name);

            if (name.isBlank()) return true;
            File file = new File(Launcher.PROJECTS_HOME, name);
            return file.exists();
        }, projectName));
    }

    @FXML
    private void onCreate() {
        Project project = new Project(
                new File(Launcher.PROJECTS_HOME, asFriendlyText(projectName.getValue())),
                productID.getValue(),
                comment.getValue(),
                language.getValue()
        );

        ProjectIO.saveProject(project, e ->
                UXUtilities.errorAlert("Unable to save general project config", e.getMessage()));

        onClose(); // TODO: Implement actual loading of project view
    }

    @FXML
    private void onClose() {
        propertySheet.getScene().getWindow().hide();
    }

    private ObservableList<PropertySheet.Item> buildConfigList() {
        ObservableList<PropertySheet.Item> items = FXCollections.observableArrayList();

        projectName = new SimpleStringProperty("");
        productID = new SimpleIntegerProperty(900);
        comment = new SimpleStringProperty("");
        language = new SimpleStringProperty("");

        items.add(new SimplePropertyItem(
                "Project Name",
                "Mandatory",
                "The name of your project directory. There cannot be two projects with the same project directory.",
                projectName));

        items.add(new SimpleNumberPropertyItem<>(
                "Product ID",
                "Mandatory",
                "The ID used by the TipToi pen to recognize this product. IDs should be unique. " +
                        "Number between 900 and 950 recommended",
                productID,
                0, 999));

        items.add(new SimplePropertyItem(
                "Comment",
                "Optional",
                "A comment for your project. This is saved in the GME file but has no affect on the TipToi execution",
                comment));

        items.add(new SimpleStringPropertyItemCompletable(
                "Language",
                "Optional",
                "The language your project is targeting. Usually, there is no need to set this for personal projects",
                language,
                "ENGLISH", "GERMAN", "DUTCH", "FRENCH", "ITALIA", "RUSSIA"));

        return items;
    }

    private static String asFriendlyText(String s) {
        return s.trim()
                .replace("/", "-")
                .replace("\\", "-")
                .replaceAll("[^a-zA-Z0-9_ -]", "")
                .trim(); // Trim here too
    }
}
