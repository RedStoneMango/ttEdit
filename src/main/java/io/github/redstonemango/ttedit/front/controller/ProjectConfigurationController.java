package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.ttedit.Launcher;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.front.UXUtilities;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.PropertySheet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ProjectConfigurationController {

    @FXML private PropertySheet propertySheet;
    @FXML private Button actionButton;
    @FXML private Label header;

    private SimpleStringProperty projectName;
    private SimpleIntegerProperty productID;
    private SimpleStringProperty comment;
    private SimpleStringProperty language;
    private SimpleMapProperty<String, Integer> initialRegisters;
    private SimpleListProperty<Sound> welcomeSounds;

    private Project project;
    private boolean createNew;

    /**
     * @param project The project to configure or {@code null} to show the 'project creation' dialog
     */
    protected void init(@Nullable Project project) {
        this.project = project;
        createNew = project == null;
        if (!createNew) header.setText("Configure '" + project.name() + "'");

        propertySheet.getScene().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                onClose();
        });

        UXUtilities.applyPropertyEditorFactory(propertySheet);
        propertySheet.getItems().addAll(buildConfigList());
        propertySheet.requestFocus();

        actionButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            if (!createNew) return false;

            String name = projectName.getValue();
            name = asFriendlyText(name);

            if (name.isBlank()) return true;
            File file = new File(Launcher.PROJECTS_HOME, name);
            return file.exists();
        }, projectName));
        actionButton.setText(createNew ? "Create" : "Done");
    }

    @FXML
    private void onAction() {
        String friendlyName = asFriendlyText(projectName.getValue());
        if (createNew) {
            Project project = new Project(
                    new File(Launcher.PROJECTS_HOME, friendlyName),
                    productID.getValue(),
                    comment.getValue(),
                    language.getValue()
            );
            project.initializeFields(friendlyName);

            ProjectIO.saveProject(project, e ->
                    UXUtilities.errorAlert("Unable to save general project config", e.getMessage()));

            onClose(); // TODO: Implement actual loading of project view
            return;
        }

        project.setProductID(productID.getValue());
        project.setComment(comment.getValue());
        project.setLanguage(language.getValue());
        project.getInitialRegisters().clear();
        project.getInitialRegisters().putAll(initialRegisters.get());
        project.getRegisterIndexUnifier().update();
        project.setWelcomeSounds(welcomeSounds.get().stream()
                .map(s -> s.soundFile().getName())
                .toList()
        );
        try {
            ProjectIO.saveProjectGeneralConfig(project);
            onClose();
        } catch (IOException e) {
            UXUtilities.errorAlert("Error saving project configuration", e.getMessage());
        }
    }

    @FXML
    private void onClose() {
        propertySheet.getScene().getWindow().hide();
    }

    private ObservableList<PropertySheet.Item> buildConfigList() {
        ObservableList<PropertySheet.Item> items = FXCollections.observableArrayList();

        projectName = new SimpleStringProperty(project != null ? project.name() : "");
        productID = new SimpleIntegerProperty(project != null ? project.getProductID() : 900);
        comment = new SimpleStringProperty(project != null ? project.getComment() : "");
        language = new SimpleStringProperty(project != null ? project.getLanguage() : "");
        initialRegisters = new SimpleMapProperty<>(
                project != null
                ? FXCollections.observableMap(new HashMap<>(project.getInitialRegisters()))
                : FXCollections.emptyObservableMap()
        );
        welcomeSounds = new SimpleListProperty<>(
                project != null
                ? FXCollections.observableArrayList(
                        project.getWelcomeSounds().stream()
                                .map(s -> Sound.fromString(s, project.getSounds()))
                                .filter(Objects::nonNull)
                                .toList())

                : FXCollections.emptyObservableList()
        );

        items.add(new SimplePropertyItem(
                "Project Name",
                "Mandatory",
                "The name of your project directory. There cannot be two projects with the same project directory.",
                projectName,
                !createNew));

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

        items.add(new SoundsPropertyItem(
                "Welcome Sounds",
                "Quality of Life",
                "The Sounds to be played when the project starts. The sounds will be played in the order " +
                        "they are specified",
                welcomeSounds,
                project != null ? project.getSounds() : FXCollections.emptyObservableList(),
                project,
                createNew
        ));

        items.add(new RegistersPropertyItem(
                "Initial Registers",
                "Scripting",
                "Initial values for registers (variables) to be initialized when the project starts",
                initialRegisters,
                project,
                createNew));

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
