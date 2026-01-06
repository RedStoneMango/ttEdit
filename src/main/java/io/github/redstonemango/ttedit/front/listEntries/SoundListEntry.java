package io.github.redstonemango.ttedit.front.listEntries;

import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

import java.io.IOException;

public class SoundListEntry {

    @FXML private Label nameLabel;
    @FXML private Circle deleteBgCircle;
    @FXML private Circle playBgCircle;
    @FXML private ImageView playImage;

    private boolean playing = false;

    void init(Sound sound, Runnable onPlayStart, Runnable onPlayStop, boolean playing, Runnable onDelete) {
        this.playing = playing;
        nameLabel.setText(sound.name());

        UXUtilities.registerHoverAnimation(deleteBgCircle, true);
        deleteBgCircle.setOnMousePressed(_ -> onDelete.run());
        UXUtilities.registerHoverAnimation(playBgCircle, true);
        playBgCircle.setOnMouseClicked(_ -> {
            this.playing = !this.playing;
            if (this.playing) {
                onPlayStart.run();
            }
            else {
                onPlayStop.run();
            }
        });
        if (playing) {
            playImage.setImage(new Image(SoundListEntry.class.getResource(
                    "/io/github/redstonemango/ttedit/image/stop.png").toExternalForm()));
        }
    }

    public static HBox build(Sound sound, Runnable onPlayStart, Runnable onPlayStop, boolean playing, Runnable onDelete,
                             ListView<?> view) {
        FXMLLoader loader = new FXMLLoader(SoundListEntry.class.getResource(
                "/io/github/redstonemango/ttedit/fxml/sound-list-entry.fxml"));

        HBox element;
        try {
            element = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SoundListEntry controller = loader.getController();
        controller.init(sound, onPlayStart, onPlayStop, playing, onDelete);

        element.prefWidthProperty().bind(view.widthProperty().subtract(20));

        return element;
    }

}
