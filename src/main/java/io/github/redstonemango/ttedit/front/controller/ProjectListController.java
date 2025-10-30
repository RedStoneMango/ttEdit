package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.Launcher;
import io.github.redstonemango.ttedit.front.UXUtilities;
import io.github.redstonemango.ttedit.front.projectEntry.ProjectEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
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
                ProjectEntry.build(file, () -> deleteProject(file), projectView)
        );

        updateProjects();
    }

    @FXML
    private void onOpenFolder() {
        OperatingSystem.loadCurrentOS().open(Launcher.PROJECTS_HOME);
    }

    @FXML
    private void onAddProject() {

    }

    private void deleteProject(File file) {

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
