package io.github.redstonemango.ttedit.back;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

public class Mp3SoundPlayer implements ISoundPlayable {

    private MediaPlayer player;

    public Mp3SoundPlayer(Sound sound) {
        try {
            player = new MediaPlayer(new Media(sound.soundFile().toURI().toString()));
        }
        catch (MediaException e) {
            System.err.println(
                    "Media exception while instantiating audio playback objects for sound '"
                            + sound.name() + "': " + e.getMessage());
            libraryErrorMessage();
            player = null;
        }
    }

    @Override
    public void play() {
        if (player == null) return;
        if (player.getStatus() != MediaPlayer.Status.UNKNOWN) {
            player.play();
        }
        else {
            player.setOnReady(() -> player.play());
        }
    }

    @Override
    public void stopPlaying() {
        if (player == null) return;
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.stop();
        }
    }

    private void libraryErrorMessage() {
        ButtonType ignoreBtn = new ButtonType("Ignore");
        ButtonType learnMoreBtn = new ButtonType("Learn More", ButtonBar.ButtonData.YES);
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ignoreBtn, learnMoreBtn);
        alert.setTitle("Playback Error");
        alert.setHeaderText("The audio playback failed");
        alert.setContentText("This might be due to a lack of libraries installed on your system");
        UXUtilities.applyStylesheet(alert);
        alert.showAndWait();
        if (alert.getResult() == learnMoreBtn) {
            OperatingSystem.loadCurrentOS().open("https://www.oracle.com/java/technologies/javase/" +
                    "products-doc-jdk8-jre8-certconfig.html#:~:text=JavaFX%20Media,12.04%20or%20equivalent.");
        }
    }
}
