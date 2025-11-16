package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.TtEdit;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.ProjectElement;
import io.github.redstonemango.ttedit.front.UXUtilities;
import io.github.redstonemango.ttedit.front.listEntries.ProjectElementListEntry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.GridView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectContentController {

    @FXML private Rectangle iconBackground;
    @FXML private Label iconText;
    @FXML private Label nameLabel;
    @FXML private HBox projectTitle;
    @FXML private HBox addScriptControl;
    @FXML private HBox addPageControl;
    @FXML private HBox editItemControl;
    @FXML private HBox cloneItemControl;
    @FXML private HBox deleteItemControl;
    @FXML private HBox configureProjectControl;
    @FXML private HBox saveProjectControl;
    @FXML private TextField filterField;
    @FXML private GridView<ProjectElement> contentView;

    private ContextMenu cxtMenu;

    private ObservableList<ProjectElement> selectedElements;
    private final ObservableList<ProjectElement> elements = FXCollections.observableArrayList();
    private final FilteredList<ProjectElement> filteredElements = new FilteredList<>(
            new SortedList<>(elements, Comparator.comparing(ProjectElement::getName)),
            _ -> true);

    @FXML
    private void initialize() {
        UXUtilities.registerHoverAnimation(projectTitle);
        UXUtilities.registerHoverAnimation(addScriptControl);
        UXUtilities.registerHoverAnimation(addPageControl);
        UXUtilities.registerHoverAnimation(editItemControl);
        UXUtilities.registerHoverAnimation(cloneItemControl);
        UXUtilities.registerHoverAnimation(deleteItemControl);
        UXUtilities.registerHoverAnimation(configureProjectControl);
        UXUtilities.registerHoverAnimation(saveProjectControl);

        Project project = Project.getCurrentProject();
        nameLabel.setText(project.name());
        iconText.setText(UXUtilities.determineAbbreviation(project.name()));
        iconBackground.setFill(UXUtilities.determineColor(project.name()));

        filterField.textProperty().addListener((_, _, filter) ->
                filteredElements.setPredicate(element ->
                        element.getName().toLowerCase().contains(filter.toLowerCase()))
        );
        contentView.setItems(filteredElements);
        contentView.setHorizontalCellSpacing(1.5);
        contentView.setVerticalCellSpacing(1.5);
        selectedElements = UXUtilities.applyCellFactoryAndSelection(contentView, ProjectElementListEntry::build);
        elements.addAll(Project.getCurrentProject().getElements());

        MenuItem configureItem = new MenuItem("Configure Project");
        configureItem.setOnAction(_ -> onConfigure());
        MenuItem saveItem = new MenuItem("Save Whole Project");
        saveItem.setOnAction(_ -> onSave());
        MenuItem folderItem = new MenuItem("Open Project Folder");
        folderItem.setOnAction(_ ->
                OperatingSystem.loadCurrentOS().open(Project.getCurrentProject().getDir()));
        MenuItem closeItem = new MenuItem("Close Project");
        closeItem.setOnAction(_ -> close());

        cxtMenu = new ContextMenu(
                configureItem, saveItem, folderItem,
                new SeparatorMenuItem(),
                closeItem
        );

        editItemControl.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> selectedElements.size() != 1,
                        selectedElements
                ));
        renameItemControl.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> selectedElements.isEmpty(),
                        selectedElements
                ));
        cloneItemControl.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> selectedElements.isEmpty(),
                        selectedElements
                ));
        deleteItemControl.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> selectedElements.isEmpty(),
                        selectedElements
                ));
    }

    @FXML
    private void onProjectTitle() {
        if (cxtMenu.isShowing()) {
            cxtMenu.hide();
        }
        else {
            Point2D titlePos = projectTitle.localToScreen(0, 0);
            cxtMenu.show(
                    projectTitle,
                    titlePos.getX() + 5,
                    titlePos.getY() + projectTitle.getHeight()
            );
        }
    }

    @FXML
    private void onAddScript() {
        mouseExit(addScriptControl);
        askName(name -> {
            ProjectElement element = new ProjectElement(unusedName(name), ProjectElement.Type.SCRIPT);

            try {
                ProjectIO.saveProjectElement(element, Project.getCurrentProject());
                elements.add(element);
                selectedElements.clear();
                selectedElements.add(element);
            } catch (IOException e) {
                UXUtilities.errorAlert("Unable to save '" + element.getName() + "'", e.getMessage());
            }
        });
    }

    @FXML
    private void onAddPage() {
        mouseExit(addPageControl);
        askName(name -> {
            ProjectElement element = new ProjectElement(unusedName(name), ProjectElement.Type.PAGE);

            try {
                ProjectIO.saveProjectElement(element, Project.getCurrentProject());
                elements.add(element);
                selectedElements.clear();
                selectedElements.add(element);
            } catch (IOException e) {
                UXUtilities.errorAlert("Unable to save '" + element.getName() + "'", e.getMessage());
            }
        });
    }

    @FXML
    private void onRename() {
        mouseExit(renameItemControl);
        boolean plural = selectedElements.size() > 1;
        String nameSuggestion = plural ? "" : selectedElements.getFirst().getName();

        askName(nameSuggestion, plural, name -> {
            Set<ProjectElement> newElements = new HashSet<>();
            selectedElements.forEach(element -> {
                elements.remove(element);
                String newName = unusedName(name, plural, newElements); // Also supply newElements because they aren't yet registered
                Project project = Project.getCurrentProject();

                File source = new File(project.getElementDir(), element.getName() + element.getType().fileSuffix());
                File target = new File(project.getElementDir(), newName + element.getType().fileSuffix());
                if (target.exists()) {
                    // This is not expected to ever happen
                    UXUtilities.errorAlert(
                            "File '" + target.getName() + "' already exists",
                            "Failed to rename element. This should not happen!"
                    );
                    return;
                }

                try {
                    Files.move(source.toPath(), target.toPath()); // Better error handling with NIO
                } catch (IOException e) {
                    UXUtilities.errorAlert("Error moving element file '" + source.getName() + "'", e.getMessage());
                    return;
                }

                try {
                    newElements.add(
                            ProjectIO.loadProjectElement(target)
                    );
                } catch (IOException e) {
                    UXUtilities.errorAlert("Error parsing new element file '" + source.getName() + "'", e.getMessage());
                }
            });

            elements.addAll(newElements);
            selectedElements.setAll(newElements);
        });
    }

    @FXML
    private void onDelete() {
        mouseExit(addPageControl);

        String name = selectedElements.size() == 1
                ? "'" + selectedElements.getFirst().getName() + "'"
                : selectedElements.size() + " elements";

        UXUtilities.confirmationAlert(
                "Do you really want to delete " + name + "?",
                name + " will be lost forever",
                () -> selectedElements.removeIf(element -> {
                    try {
                        ProjectIO.deleteProjectElement(element, Project.getCurrentProject());
                        elements.remove(element);
                        return true; // Remove from 'selected elements' list
                    } catch (IOException e) {
                        UXUtilities.errorAlert("Error deleting '" + element.getName() + "'", e.getMessage());
                        return false; // Do not remove from 'selected elements' list because action failed
                    }
                }));
    }

    @FXML
    private void onClone() {
        mouseExit(cloneItemControl);
        boolean plural = selectedElements.size() > 1;
        String nameSuggestion = plural ? "" : selectedElements.getFirst().getName();

        askName(nameSuggestion, plural, name -> {
            Set<ProjectElement> newElements = new HashSet<>();
            selectedElements.forEach(element -> {
                String newName = unusedName(name, plural, newElements); // Also supply newElements because they aren't yet registered
                Project project = Project.getCurrentProject();

                File source = new File(project.getElementDir(), element.getName() + element.getType().fileSuffix());
                File target = new File(project.getElementDir(), newName + element.getType().fileSuffix());
                if (target.exists()) {
                    // This is not expected to ever happen
                    UXUtilities.errorAlert(
                            "File '" + target.getName() + "' already exists",
                            "Failed to clone element. This should not happen!"
                    );
                    return;
                }

                try {
                    Files.copy(source.toPath(), target.toPath()); // Better error handling with NIO
                } catch (IOException e) {
                    UXUtilities.errorAlert("Error copying element file '" + source.getName() + "'", e.getMessage());
                    return;
                }

                try {
                    newElements.add(
                            ProjectIO.loadProjectElement(target)
                    );
                } catch (IOException e) {
                    UXUtilities.errorAlert("Error parsing new element file '" + source.getName() + "'", e.getMessage());
                }
            });

            elements.addAll(newElements);
            selectedElements.setAll(newElements);
        });
    }

    @FXML
    private void onConfigure() {
        mouseExit(configureProjectControl);

        Project project = Project.getCurrentProject();
        Stage stage = new Stage();
        stage.setTitle("Configure '" + project.name() + "'");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(contentView.getScene().getWindow());
        Scene scene;

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/io/github/redstonemango/ttedit/fxml/project-configuration.fxml"));
        try {
            scene = new Scene(loader.load());
            ProjectConfigurationController controller = loader.getController();
            controller.init(project);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UXUtilities.applyStylesheet(scene);
        UXUtilities.defineMinSize(stage);

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onSave() {
        mouseExit(saveProjectControl);

        AtomicBoolean success = new AtomicBoolean(true);
        ProjectIO.saveProject(Project.getCurrentProject(), e -> {
            success.set(false);
            UXUtilities.errorAlert("Error saving project", e.getMessage());
        });

        if (success.get()) {
            UXUtilities.informationAlert("Save successful", "Your whole project has been successfully saved!");
        }
    }

    private void mouseExit(Node node) {
        if (node.getOnMouseExited() != null)
            node.getOnMouseExited().handle(null); // Just call the handle function. Event is unused in this context
    }

    private void askName(Consumer<String> nameAction){
        askName("", false, nameAction);
    }

    private void askName(String suggestion, boolean plural, Consumer<String> nameAction) {
        TextInputDialog dialog = new TextInputDialog(suggestion);
        dialog.setTitle("Set Name");
        dialog.setHeaderText("Please enter a name for the element" + (plural ? "s" : ""));
        dialog.setContentText("Name: ");
        UXUtilities.applyStylesheet(dialog);
        dialog.showAndWait();
        if (dialog.getResult() != null) {
            nameAction.accept(dialog.getResult());
        }
    }

    private String unusedName(String name) {
        return unusedName(name, false, new HashSet<>());
    }

    private String unusedName(String name, boolean denyBaseOnly, Set<ProjectElement> additionalElements) {
        String basename = name;
        int i = 1;
        if (denyBaseOnly) {
            name = basename + "_1";
        }
        Set<String> existingNames =
                Stream.concat(
                        elements.stream(),
                        additionalElements.stream()
                )
                .map(ProjectElement::getName)
                .collect(Collectors.toSet());

        while (existingNames.contains(name)) {
            name = basename + "_" + i;
            i++;
        }
        return name;
    }

    private void close() {
        Stage stage = TtEdit.getPrimaryStage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/io/github/redstonemango/ttedit/fxml/project-list.fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Platform.runLater(() -> {
            UXUtilities.applyStylesheet(scene);
            stage.setTitle("ttEdit");
            stage.setMaximized(false);
            stage.setScene(scene);

            UXUtilities.defineMinSize(stage);
        });
    }
}
