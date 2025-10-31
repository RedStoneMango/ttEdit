package io.github.redstonemango.ttedit.front.listEntries;

import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;

public class ProjectListEntry {

    @FXML private Label nameLabel;
    @FXML private Circle deleteBgCircle;

    void init(File file, Runnable onDelete) {
        nameLabel.setText(file.getName());

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
