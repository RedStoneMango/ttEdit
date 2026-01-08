package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.mangoutils.OperatingSystem;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.back.projectElement.BranchCondition;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.back.registerDictionary.RegisterIndexUnifier;
import io.github.redstonemango.ttedit.front.propertySheetHelpers.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UXUtilities {

    private static final String STYLE_SHEET =
            UXUtilities.class.getResource("/io/github/redstonemango/ttedit/style/application.css").toExternalForm();
    private static final Pattern POSITIVE_INT_PATTERN = Pattern.compile("^\\d+$");

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
        scene.getStylesheets().add(STYLE_SHEET);
    }

    public static void applyStylesheet(Alert alert) {
        alert.getDialogPane().getStylesheets().add(STYLE_SHEET);
    }

    public static void applyStylesheet(TextInputDialog dialog) {
        dialog.getDialogPane().getStylesheets().add(STYLE_SHEET);
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

    public static <T> void doOnceAvailable(ObservableValue<T> property, Consumer<T> action) {
        if (property.getValue() != null) {
            action.accept(property.getValue());
            return;
        }

        AtomicReference<ChangeListener<? super T>> l = new AtomicReference<>();

        l.set((_, _, val) -> {
            if (val != null) {
                action.accept(val);
                property.removeListener(l.get());
            }
        });

        property.addListener(l.get());
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
        confirmationAlert(heading, content, onAction, () -> {});
    }

    public static void confirmationAlert(String heading, String content, Runnable onAction, Runnable onDone) {
        runOnApplicationThread(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Required");
            alert.setHeaderText(heading);
            alert.setContentText(content);
            applyStylesheet(alert);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) onAction.run();
            onDone.run();
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

    public static void applyRegisterCompletion(TextField field, ProjectElement element,
                                               RegisterIndexUnifier registerIndexUnifier, boolean allowNumber) {
        TextFields.bindAutoCompletion(field, s -> registerIndexUnifier.getRegisters().stream()
                .filter(r ->
                        r.toLowerCase().contains(s.getUserText().toLowerCase())
                        &&
                        !r.equals(s.getUserText()))
                .collect(Collectors.toSet()));

        field.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) {
                String s = ensureRegisterSyntax(field.getText(), allowNumber);
                field.setText(s);
                registerIndexUnifier.getLiveIndex().updateEntry(field, element, s);
            }
        });
        // Init entry
        registerIndexUnifier.getLiveIndex().updateEntry(field, element, field.getText());
    }

    private static String ensureRegisterSyntax(String text, boolean allowNumber) {
        if (!allowNumber) {
            return ScriptData.forceRegisterPattern(text);
        }

        if (text.isBlank()) {
            return "1";
        }

        if (!POSITIVE_INT_PATTERN.matcher(text).matches()) {
            return ScriptData.forceRegisterPattern(text);
        }

        if (text.length() > 5) {
            text = text.substring(0, 5);
        }

        int value = (int) Long.parseLong(text); // "String -> long -> int" to correctly handle strings in "65536..99999"
        value = Math.clamp(value, 0, 65535);

        return String.valueOf(value);
    }

    public static void applyReadonlyRegisterCompletion(TextField field, RegisterIndexUnifier registerIndexUnifier,
                                                       Supplier<Collection<String>> additionalRegistersSupplier) {
        TextFields.bindAutoCompletion(field, s -> Stream.concat(
                    registerIndexUnifier.getRegisters().stream(),
                    additionalRegistersSupplier.get().stream()
                ).filter(r ->
                        r.toLowerCase().contains(s.getUserText().toLowerCase())
                                &&
                                !r.equals(s.getUserText()))
                .collect(Collectors.toSet()));
    }

    public static void applyActionComboBoxCellFactory(ComboBox<ScriptData.Action> comboBox) {
        comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ScriptData.Action> call(ListView<ScriptData.Action> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ScriptData.Action item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText(item.getLiteral());
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ScriptData.Action item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(item.getLiteral());
                }
            }
        });
    }
    public static void applyComparisonComboBoxCellFactory(ComboBox<BranchCondition.Comparison> comboBox) {
        comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<BranchCondition.Comparison> call(ListView<BranchCondition.Comparison> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(BranchCondition.Comparison item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText(item.getLiteral());
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BranchCondition.Comparison item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(item.getLiteral());
                }
            }
        });
    }
    public static void applyProjectElementComboBoxCellFactory(ComboBox<ProjectElement> comboBox) {
        comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ProjectElement> call(ListView<ProjectElement> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ProjectElement item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProjectElement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(item.getName());
                }
            }
        });
    }

    public static <T> void applyCustomCellFactory(ListView<T> listView, Function<T, Node> nodeFunction,
                                                      Consumer<T> onDoubleClick, Insets padding) {
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(nodeFunction.apply(item));
                            setPadding(padding);
                            setOnMouseClicked(e -> {
                                if (e.getClickCount() >= 2) {
                                    onDoubleClick.accept(getItem());
                                }
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
            else if (item instanceof RegistersPropertyItem registers) {
                editor = new RegistersPropertyEditor(registers);
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

    public static Callback<ListView<Sound>, ListCell<Sound>> createSoundListCellFactory(boolean smallSized) {
        return new Callback<>() {
            @Override
            public ListCell<Sound> call(ListView<Sound> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Sound sound, boolean empty) {
                        super.updateItem(sound, empty);
                        if (empty || sound == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            Label name = new Label(sound.name());
                            name.setFont(new Font(smallSized ? 15 : 19));
                            Label file = new Label(sound.soundFile().getName());
                            file.setFont(new Font(smallSized ? 10 : 14));
                            VBox box = new VBox(name, file);
                            setGraphic(box);
                            setPadding(new Insets(5));
                        }
                    }
                };
            }
        };
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

    public static <T> ObservableList<T> applyCellFactoryAndSelection(GridView<T> gridView, Function<T, Node> nodeFunction,
                                                                     Consumer<T> doubleClicked) {
        ObservableList<T> selectedItems = FXCollections.observableArrayList();

        AtomicInteger lastSelectedIndex = new AtomicInteger(-1);

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
                    lastSelectedIndex.set(index);

                } else if (event.isShiftDown() && lastSelectedIndex.get() >= 0 && lastSelectedIndex.get() != index) {
                    int start = Math.min(lastSelectedIndex.get(), index);
                    int end = Math.max(lastSelectedIndex.get(), index);
                    selectedItems.clear();
                    selectedItems.addAll(gridView.getItems().subList(start, end + 1));

                } else if (lastSelectedIndex.get() != index) {
                    selectedItems.clear();
                    selectedItems.add(cell.getItem());
                    lastSelectedIndex.set(index);
                }

                cell.pseudoClassStateChanged(
                        PseudoClass.getPseudoClass("selected"),
                        selectedItems.contains(cell.getItem())
                );

                if (event.getClickCount() >= 2) {
                    doubleClicked.accept(cell.getItem());
                }
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

    public static @Nullable List<Sound> showAddSoundUI(Project project, Window window) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Audio File", "*.mp3"));
        chooser.setTitle("Add Sound");
        var files = chooser.showOpenMultipleDialog(window);
        if (files != null) {
            List<Sound> addedSounds = new ArrayList<>();
            for (File file : files) {
                if (!file.getName().endsWith(".mp3")) {
                    errorAlert("Unsupported file", file.getName() + " is not supported. Use .mp3 files only");
                    continue;
                }
                var s = ProjectIO.addSound(project, file, e -> errorAlert("Unable to add sound", e.getMessage()));
                if (s != null) {
                    addedSounds.add(s);
                }
            }
            return addedSounds;
        }
        return null;
    }

}
