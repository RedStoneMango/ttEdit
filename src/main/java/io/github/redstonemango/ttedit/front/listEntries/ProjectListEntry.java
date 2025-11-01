package io.github.redstonemango.ttedit.front.listEntries;

import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ProjectListEntry {

    @FXML private Label nameLabel;
    @FXML private Circle deleteBgCircle;
    @FXML private Rectangle iconBackground;
    @FXML private Label iconText;

    void init(File file, Runnable onDelete) {
        String name = file.getName();
        nameLabel.setText(name);
        iconText.setText(UXUtilities.determineAbbreviation(name));
        iconBackground.setFill(UXUtilities.determineColor(name));


        UXUtilities.registerHoverAnimation(deleteBgCircle, true);
        deleteBgCircle.setOnMousePressed(_ -> onDelete.run());
    }

    public static HBox build(File file, Runnable onDelete, ListView<?> view) {
        FXMLLoader loader = new FXMLLoader(ProjectListEntry.class.getResource(
                "/io/github/redstonemango/ttedit/fxml/project-list-entry.fxml"));

        HBox element;
        try {
            element = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ProjectListEntry controller = loader.getController();
        controller.init(file, onDelete);

        element.prefWidthProperty().bind(view.widthProperty().subtract(20));

        return element;
    }

}
