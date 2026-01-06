package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ProjectSoundManageController {

    @FXML private Label header;
    @FXML private ListView<Sound> soundsView;
    @FXML private Button playButton;
    @FXML private Button deleteButton;

    public void init(Project project) {
        header.setText("Mange Sounds for '" + project.name() + "'");
        soundsView.setItems(project.getSounds());
        UXUtilities.applyCustomCellFactory(soundsView, sound -> {
            Label name = new Label(sound.name());
            name.setFont(new Font(19));
            Label file = new Label(sound.soundFile().getName());
            file.setFont(new Font(14));
            VBox box = new VBox(name, file);
            box.setPadding(new Insets(5));
            return box;
        }, _ -> {}, new Insets(0));
    }

    @FXML
    private void onPlay() {

    }
    @FXML
    private void onDelete() {

    }
    @FXML
    private void onAdd() {

    }
}
