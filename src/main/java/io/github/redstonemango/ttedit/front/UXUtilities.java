package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Set;
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

    public static void applyStylesheet(TextInputDialog dialog) {
        dialog.getDialogPane().getStylesheets().add(styleSheet);
    }

    public static void defineMinSize(Stage stage) {
        defineMinSize(stage, true);
    }

    public static void defineMinSize(Stage stage, boolean runLater) {
        Runnable r = () -> {
            double decoration = stage.getHeight() - stage.getScene().getHeight();
            stage.setMinWidth(stage.getScene().getWidth());
            stage.setMinHeight(stage.getScene().getHeight() + decoration);
        };
        if (runLater) Platform.runLater(r);
        else r.run();
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
            PropertyEditor<?> editor = null;

            if (item instanceof SimpleNumberPropertyItem<?> ranged) {
                Class<?> type = item.getType();
                if (Number.class.isAssignableFrom(type) ||
                        int.class.isAssignableFrom(type) ||
                        double.class.isAssignableFrom(type)) {

                    editor = new SliderPropertyEditor(item, ranged.getMin(), ranged.getMax(),
                            !Integer.class.isAssignableFrom(type) && !int.class.isAssignableFrom(type));
                }
            }
            else if (item instanceof SimpleStringPropertyItemCompletable completable) {
                editor = new CompletableFieldPropertyEditor(item, completable.getCompletions());
            }
            else if (item instanceof SimpleStringPropertyItemLinked linked) {
                editor = new LinkedFieldPropertyEditor(item, linked.getSource(), linked.getConversionCallback());
            }
            else if (item instanceof SimplePropertyItem) {
                editor = Editors.createTextEditor(item);
            }

            if (editor != null) {
                SimplePropertyItem simpleItem = (SimplePropertyItem) item;
                editor.getEditor().setDisable(simpleItem.isDisabled());
                return editor;
            }

            return defaultFactory.call(item);
        });
    }

    public static void registerHoverAnimation(Node node) {
        registerHoverAnimation(node, false);
    }

    public static void registerHoverAnimation(Node node, boolean inverted) {
        node.setOnMouseEntered(_ -> {
            FadeTransition transition = new FadeTransition(Duration.millis(250), node);
            transition.setFromValue(inverted ? 0.45 : 1);
            transition.setToValue(inverted ? 1 : 0.45);
            transition.play();
        });
        node.setOnMouseExited(_ -> {
            if (node.getOpacity() == (inverted ? 0.45 : 1)) return; // Prevent re-play if unnecessary
            FadeTransition transition = new FadeTransition(Duration.millis(250), node);
            transition.setFromValue(inverted ? 1 : 0.45);
            transition.setToValue(inverted ? 0.45 : 1);
            transition.play();
        });
    }


    public static String determineAbbreviation(String name) {
        Set<Character> separators = Set.of(' ', '-', '_');
        boolean afterSeparator = false;
        StringBuilder abbreviation = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (abbreviation.isEmpty()) {
                if (Character.isLetter(c)) abbreviation.append(c);
            }
            else {
                if (afterSeparator && Character.isLetter(c)) {
                    abbreviation.append(c);
                    break;
                }

                if (separators.contains(c)) afterSeparator = true;
            }
        }
        return abbreviation.toString().toUpperCase();
    }

    public static Paint determineColor(String str) {
        final Paint[] COLORS = {
                Color.LIGHTGRAY, Color.BROWN, Color.DARKCYAN, Color.MEDIUMORCHID,
                Color.TOMATO, Color.MEDIUMPURPLE, Color.MAGENTA, Color.FORESTGREEN,
                Color.SPRINGGREEN, Color.OLIVE, Color.GOLD, Color.STEELBLUE,
                Color.ROYALBLUE, Color.LIGHTSLATEGRAY, Color.TURQUOISE, Color.PERU
        };

        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * 31 + str.charAt(i));
        }
        return COLORS[hash & 0x0F]; // lowest 4 bits → 0..15
    }

    public static <T> ObservableList<T> applyCellFactoryAndSelection(GridView<T> gridView, Function<T, Node> nodeFunction) {
        ObservableList<T> selectedItems = FXCollections.observableArrayList();

        final int[] lastSelectedIndex = {-1};

        gridView.setCellFactory(_ -> {
            GridCell<T> cell = new GridCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);

                        pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
                    } else {
                        setText(null);
                        setGraphic(nodeFunction.apply(item));

                        pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), selectedItems.contains(getItem()));
                    }
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (cell.getItem() == null || event.getButton() != MouseButton.PRIMARY) {
                    return;
                }

                int index = cell.getIndex();

                boolean controlDown = OperatingSystem.isMac() ? event.isMetaDown() : event.isControlDown();
                if (controlDown) {
                    if (selectedItems.contains(cell.getItem())) {
                        selectedItems.remove(cell.getItem());
                    } else {
                        selectedItems.add(cell.getItem());
                    }
                    lastSelectedIndex[0] = index;

                } else if (event.isShiftDown() && lastSelectedIndex[0] >= 0) {
                    int start = Math.min(lastSelectedIndex[0], index);
                    int end = Math.max(lastSelectedIndex[0], index);
                    selectedItems.clear();
                    selectedItems.addAll(gridView.getItems().subList(start, end + 1));

                } else {
                    selectedItems.clear();
                    selectedItems.add(cell.getItem());
                    lastSelectedIndex[0] = index;
                }

                cell.pseudoClassStateChanged(
                        PseudoClass.getPseudoClass("selected"),
                        selectedItems.contains(cell.getItem())
                );
            });

            return cell;
        });
        selectedItems.addListener((ListChangeListener<? super T>) _ -> {
            ObservableList<T> items = gridView.getItems();
            gridView.setItems(FXCollections.observableArrayList());
            gridView.setItems(items);
        });

        return selectedItems;
    }

}
