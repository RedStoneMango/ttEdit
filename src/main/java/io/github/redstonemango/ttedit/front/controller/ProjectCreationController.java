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

    private SimpleStringProperty locationName;
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
                    if (locationName.getValue().isBlank()) return true;
                    File file = new File(Launcher.PROJECTS_HOME, locationName.getValue());
                    return file.exists();
                })
        );
    }

    @FXML
    private void onCreate() {
        Project project = new Project(
                new File(Launcher.PROJECTS_HOME, locationName.getValue()),
                productID.getValue(),
                comment.getValue(),
                language.getValue()
        );

        try {
            ProjectIO.saveProjectGeneralConfig(project);
        } catch (IOException e) {
            UXUtilities.errorAlert("Unable to save general project config", e.getMessage());
        }

        onClose(); // TODO: Implement actual loading of project view
    }

    @FXML
    private void onClose() {
        propertySheet.getScene().getWindow().hide();
    }

    private ObservableList<PropertySheet.Item> buildConfigList() {
        ObservableList<PropertySheet.Item> items = FXCollections.observableArrayList();

        locationName = new SimpleStringProperty("");
        productID = new SimpleIntegerProperty(900);
        comment = new SimpleStringProperty("");
        language = new SimpleStringProperty("");

        items.add(new SimplePropertyItem(
                "Location Name",
                "Mandatory",
                "The name of the directory containing your project data. Usually this will be equal to your project's name",
                locationName));

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
                .replace(" ", "_")
                .replace("/", "-")
                .replace("\\", "-")
                .replaceAll("[^a-zA-Z0-9_-]", "")
                .trim(); // Trim here too
    }
}
