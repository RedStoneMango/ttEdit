package io.github.redstonemango.ttedit.front.controller;

import io.github.redstonemango.ttedit.back.ISoundPlayable;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Set;
import java.util.stream.Collectors;

public class ProjectSoundManageController {

    @FXML private Label header;
    @FXML private ListView<Sound> soundsView;
    @FXML private Button playButton;
    @FXML private Button renameButton;
    @FXML private Button deleteButton;

    private final ObjectProperty<ISoundPlayable> activeSoundPlayer = new SimpleObjectProperty<>(null);

    public void init(Project project) {
        header.setText("Manage Sounds for '" + project.name() + "'");
        soundsView.setItems(project.getSounds());
        soundsView.setCellFactory(UXUtilities.createSoundListCellFactory());

        renameButton.disableProperty().bind(deleteButton.disableProperty());
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> soundsView.getSelectionModel().getSelectedItem() == null,
                soundsView.getSelectionModel().selectedItemProperty())
        );
        playButton.disableProperty().bind(deleteButton.disableProperty().and(activeSoundPlayer.isNull()));

        UXUtilities.doOnceAvailable(playButton.getScene().windowProperty(), window -> {
            window.showingProperty().addListener((_, _, isShowing) -> {
                if (!isShowing && activeSoundPlayer.get() != null) {
                    activeSoundPlayer.get().stopPlaying();
                    activeSoundPlayer.set(null);
                }
            });
            window.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    window.hide();
                }
            });
        });
    }

    @FXML
    private void onPlay() {
        if (activeSoundPlayer.get() != null) {
            activeSoundPlayer.get().stopPlaying();
            activeSoundPlayer.set(null);
            playButton.setText("Play");
            return;
        }

        Sound sound = soundsView.getSelectionModel().getSelectedItem();
        if (sound == null) return;

        var player = sound.getPlayer();
        player.setOnPlaybackEnd(() -> {
            playButton.setText("Play");
            activeSoundPlayer.set(null);
        });
        player.play();
        activeSoundPlayer.set(player);
        playButton.setText("Stop");
    }
    @FXML
    private void onRename() {
        Sound sound = soundsView.getSelectionModel().getSelectedItem();
        if (sound == null) return;

        TextInputDialog dialog = new TextInputDialog(sound.name());
        dialog.setTitle("Rename");
        dialog.setHeaderText("Please enter a name for the sound");
        dialog.setContentText("Name: ");
        UXUtilities.applyStylesheet(dialog);
        dialog.showAndWait();
        if (dialog.getResult() != null) {
            String name = dialog.getResult();
            int i = 1;
            Set<String> names = soundsView.getItems().stream()
                    .filter(s -> s != sound)
                    .map(s -> s.soundFile().getName())
                    .collect(Collectors.toSet());
            while (names.contains(name + ".mp3")) {
                name = "_" + i;
                i++;
            }
            ProjectIO.renameSound(
                    Project.getCurrentProject(),
                    sound,
                    name + ".mp3",
                    e -> UXUtilities.errorAlert("Error renaming sound", e.getMessage())
            );
            soundsView.refresh();
        }
    }
    @FXML
    private void onDelete() {
        Sound sound = soundsView.getSelectionModel().getSelectedItem();
        if (sound == null) return;

        UXUtilities.confirmationAlert(
                "Delete Sound",
                "Do you really want to delete '" + sound.name() + "'?",
                () -> {
                    ProjectIO.removeSound(
                            Project.getCurrentProject(),
                            sound,
                            e -> UXUtilities.errorAlert("Error deleting sound", e.getMessage())
                    );
                    soundsView.refresh();
                });
    }
    @FXML
    private void onAdd() {
        UXUtilities.showAddSoundUI(Project.getCurrentProject(), playButton.getScene().getWindow());
        soundsView.refresh();
    }
}
