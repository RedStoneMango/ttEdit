package io.github.redstonemango.ttedit.front;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

public class UXUtilities {

    private static String styleSheet =
            UXUtilities.class.getResource("/io/github/redstonemango/ttedit/style/application.css").toExternalForm();

    public static void informationAlert(String heading, String content) {
        runOnApplicationThread(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(heading);
            alert.setContentText(content);
            applyStylesheet(alert);
            alert.show();
        });
    }

    public static void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(styleSheet);
    }

    public static void applyStylesheet(Alert alert) {
        alert.getDialogPane().getStylesheets().add(styleSheet);
    }

    public static void errorAlert(String heading, String content) {
        errorAlert(heading, content, true);
    }

    public static void errorAlert(String heading, String content, boolean logError) {
        runOnApplicationThread(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(heading);
            alert.setContentText(content);
            applyStylesheet(alert);
            alert.show();

            if (logError) System.err.println(heading + " > " + content);
        });
    }

    public static void runOnApplicationThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        }
        else {
            Platform.runLater(action);
        }
    }

}
