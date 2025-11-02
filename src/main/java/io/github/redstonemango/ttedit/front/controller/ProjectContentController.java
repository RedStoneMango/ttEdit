package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

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
    }

}
