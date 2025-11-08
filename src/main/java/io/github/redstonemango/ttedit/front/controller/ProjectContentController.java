package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.TtEdit;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.front.UXUtilities;
import io.github.redstonemango.ttedit.front.listEntries.ProjectElementListEntry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            ProjectElement element = new ProjectElement(name, ProjectElement.Type.SCRIPT);

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
            ProjectElement element = new ProjectElement(name, ProjectElement.Type.PAGE);

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

    private void askName(Consumer<String> nameAction) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Element");
        dialog.setHeaderText("Please set a name for the new element");
        dialog.setContentText("Name: ");
        UXUtilities.applyStylesheet(dialog);
        dialog.showAndWait();
        if (dialog.getResult() != null) {
            String basename = dialog.getResult();
            String name = dialog.getResult();
            int i = 1;
            Set<String> existingNames = elements
                    .stream()
                    .map(ProjectElement::getName)
                    .collect(Collectors.toSet());

            while (existingNames.contains(name)) {
                name = basename + "_" + i;
                i++;
            }

            nameAction.accept(name);
        }
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
