package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.mangoutils.MangoIO;
import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.Launcher;
import io.github.redstonemango.ttedit.front.UXUtilities;
import io.github.redstonemango.ttedit.front.listEntries.ProjectListEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ProjectListController {

    @FXML private ListView<File> projectView;
    @FXML private TextField filterField;

    ObservableList<File> projects = FXCollections.observableArrayList();
    FilteredList<File> filteredProjects = new FilteredList<>(projects, File::isDirectory);

    @FXML
    private void initialize() {
        filterField.textProperty().addListener((_, _, filter) ->
            filteredProjects.setPredicate(file -> file.isDirectory()
                    && file.getName().toLowerCase().contains(filter.toLowerCase()))
        );
        projectView.setItems(filteredProjects);

        projectView.sceneProperty().addListener((_, _, sc) -> {
            if (sc != null) {
                sc.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    boolean controlDown = OperatingSystem.isMac() ? e.isMetaDown() : e.isControlDown();
                    if (e.getCode() == KeyCode.R && controlDown) {
                        updateProjects();
                    }
                });
            }
        });

        UXUtilities.applyCustomCellFactory(projectView, file ->
                ProjectListEntry.build(file, () -> deleteProject(file), projectView)
        );

        updateProjects();
    }

    @FXML
    private void onOpenFolder() {
        OperatingSystem.loadCurrentOS().open(Launcher.PROJECTS_HOME);
    }

    @FXML
    private void onAddProject() {
        Stage stage = new Stage();
        stage.setTitle("Create New Project");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(projectView.getScene().getWindow());
        Scene scene;

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/io/github/redstonemango/ttedit/fxml/project-creation.fxml"));
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UXUtilities.applyStylesheet(scene);

        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> {
            stage.setMinWidth(scene.getWidth());
            stage.setMinHeight(scene.getHeight());
        });
    }

    private void deleteProject(File file) {
        UXUtilities.confirmationAlert(
                "Delete '" + file.getName() + "'",
                "Do you really want to delete this project? All data will be lost forever.",
                () -> {
                    try {
                        MangoIO.deleteDirectoryRecursively(file);
                    } catch (IOException e) {
                        UXUtilities.errorAlert(
                                "Error deleting project '" + file.getName() + "'",
                                String.valueOf(e)
                        );
                    }
                    updateProjects();
                }
        );
    }

    private void updateProjects() {
        projects.setAll(
                Arrays.stream(
                        Objects.requireNonNullElse(
                                Launcher.PROJECTS_HOME.list(),
                                new String[0])
                        )
                    .map(s -> new File(Launcher.PROJECTS_HOME, s))
                    .filter(File::isDirectory)
                    .toList()
        );
    }
}
