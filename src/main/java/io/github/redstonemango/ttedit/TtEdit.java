package io.github.redstonemango.ttedit;

import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TtEdit extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/io/github/redstonemango/ttedit/fxml/project-list.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        UXUtilities.applyStylesheet(scene);
        stage.setTitle("ttEdit");
        stage.setScene(scene);
        stage.show();
        primaryStage = stage;

        UXUtilities.defineMinSize(stage);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
