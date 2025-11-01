package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.front.propertySheetHelpers.CompletableFieldPropertyEditor;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.SimpleNumberPropertyItem;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.SimpleStringPropertyItemCompletable;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.SliderPropertyEditor;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public static void defineMinSize(Stage stage) {
        Platform.runLater(() -> {
            stage.setMinWidth(stage.getScene().getWidth());
            stage.setMinHeight(stage.getScene().getHeight());
        });
    }

    public static void doOnceSceneLoads(Node node, Consumer<Scene> action) {
        AtomicReference<ChangeListener<? super Scene>> l = new AtomicReference<>();

        l.set((_, _, scene) -> {
            if (scene != null) {
                action.accept(scene);
                node.sceneProperty().removeListener(l.get());
            }
        });

        node.sceneProperty().addListener(l.get());
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

    public static void warningAlert(String heading, String content) {
        runOnApplicationThread(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(heading);
            alert.setContentText(content);
            applyStylesheet(alert);
            alert.show();
        });
    }

    public static void confirmationAlert(String heading, String content, Runnable onAction) {
        runOnApplicationThread(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Please confirm");
            alert.setHeaderText(heading);
            alert.setContentText(content);
            applyStylesheet(alert);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) onAction.run();
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

    public static <T> void applyCustomCellFactory(ListView<T> listView, Function<T, Node> nodeFunction) {
        applyCustomCellFactory(listView, nodeFunction, _ -> {}, new Insets(0));
    }

    public static <T> void applyCustomCellFactory(ListView<T> listView, Function<T, Node> nodeFunction,
                                                      Consumer<T> onDoubleClick, Insets padding) {
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> lv) {
                return new ListCell<>() {

                    private long lastClick = -1;

                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(nodeFunction.apply(item));
                            setPadding(padding);
                            setOnMouseClicked(_ -> {
                                if (System.currentTimeMillis() - lastClick <= 250) onDoubleClick.accept(getItem());
                                lastClick = System.currentTimeMillis();
                            });
                        }
                    }
                };
            }
        });
    }

    public static void applyPropertyEditorFactory(PropertySheet propertySheet) {
        DefaultPropertyEditorFactory defaultFactory = new DefaultPropertyEditorFactory();
        propertySheet.setPropertyEditorFactory(item -> {
            if (item instanceof SimpleNumberPropertyItem<?> ranged) {
                Class<?> type = item.getType();
                if (Number.class.isAssignableFrom(type) ||
                        int.class.isAssignableFrom(type) ||
                        double.class.isAssignableFrom(type)) {

                    return new SliderPropertyEditor(item, ranged.getMin(), ranged.getMax(),
                            !Integer.class.isAssignableFrom(type) && !int.class.isAssignableFrom(type));
                }
            }

            else if (item instanceof SimpleStringPropertyItemCompletable completable) {
                return new CompletableFieldPropertyEditor(item, completable.getCompletions());
            }

            return defaultFactory.call(item);
        });
    }

    public static void registerHoverAnimation(Node node) {
        registerHoverAnimation(node, false);
    }

    public static void registerHoverAnimation(Node node, boolean inverted) {
        node.getParent().setOnMouseEntered(_ -> {
            FadeTransition transition = new FadeTransition(Duration.millis(250), node);
            transition.setFromValue(inverted ? 0.45 : 1);
            transition.setToValue(inverted ? 1 : 0.45);
            transition.play();
        });
        node.getParent().setOnMouseExited(_ -> {
            FadeTransition transition = new FadeTransition(Duration.millis(250), node);
            transition.setFromValue(inverted ? 1 : 0.45);
            transition.setToValue(inverted ? 0.45 : 1);
            transition.play();
        });
    }

}
