package io.github.redstonemango.ttedit;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.back.TttoolSubprocess;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class ttEdit extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if (checkTttool()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/io/github/redstonemango/ttedit/fxml/project-list.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            UXUtilities.applyStylesheet(scene);
            stage.setTitle("ttEdit");
            stage.setScene(scene);
            stage.show();

            Platform.runLater(() -> {
                stage.setMinWidth(scene.getWidth());
                stage.setMinHeight(scene.getHeight());
            });
        }
    }

    private boolean checkTttool() {
        if (TttoolSubprocess.findTttool() == null) {
            System.err.println("Unable to find an installation of tttool in the PATH! Application cannot start correctly.");

            ButtonType infoButton = new ButtonType("Installation Guide");
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alert = new Alert(Alert.AlertType.ERROR, "", infoButton, closeButton);
            alert.setTitle("No TTTool found");
            alert.setHeaderText("Could not find an installation of TTTool in the PATH. This is required to start");
            alert.setContentText("Please make sure you have a valid TTTool installation named 'tttool' in your system PATH");
            UXUtilities.applyStylesheet(alert);
            alert.showAndWait();
            if (alert.getResult() == infoButton) {
                OperatingSystem.loadCurrentOS().open(
                        "https://github.com/RedStoneMango/ttEdit/blob/main/InstallingTttool.md#-" +
                                (OperatingSystem.isWindows() ? "Windows" : (OperatingSystem.isMac() ? "macOS" : "Linux")));
            }

            Platform.exit();
            return false;
        }
        return true;
    }
}
