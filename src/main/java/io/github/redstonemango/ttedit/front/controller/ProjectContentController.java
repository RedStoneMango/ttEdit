package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.ttedit.TtEdit;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

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

    private ContextMenu cxtMenu;

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

        MenuItem configureItem = new MenuItem("Configure Project");
        configureItem.setOnAction(_ -> onConfigure());
        MenuItem saveItem = new MenuItem("Save Whole Project");
        saveItem.setOnAction(_ -> onSave());
        MenuItem closeItem = new MenuItem("Close Project");
        closeItem.setOnAction(_ -> {
            Platform.runLater(this::close);
        });

        cxtMenu = new ContextMenu(
                configureItem, saveItem,
                new SeparatorMenuItem(),
                closeItem
        );
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
    private void onConfigure() {

    }

    @FXML
    private void onSave() {

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
