package io.github.redstonemango.ttedit.front.listEntries;

import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.GridView;

import java.io.IOException;

public class ProjectElementListEntry {

    @FXML private Label nameLabel;
    @FXML private ImageView iconImage;

    void init(ProjectElement element) {
        nameLabel.setText(element.getName());
        iconImage.setImage(element.getType().buildImage());
    }

    public static HBox build(ProjectElement source) {
        FXMLLoader loader = new FXMLLoader(ProjectListEntry.class.getResource(
                "/io/github/redstonemango/ttedit/fxml/project-element-list-entry.fxml"));

        HBox element;
        try {
            element = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ProjectElementListEntry controller = loader.getController();
        controller.init(source);

        return element;
    }

}
