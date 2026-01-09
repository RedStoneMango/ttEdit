package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.mangoutils.tuple.Tuple2;
import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.ProjectIO;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.IElementEditable;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ScriptElementEditor extends HBox implements IElementEditable {

    public static final Image BIN_CLOSED = new Image(
            ScriptElementEditor.class.getResource("/io/github/redstonemango/ttedit/image/bin_closed.png").toExternalForm());
    public static final Image BIN_OPEN = new Image(
            ScriptElementEditor.class.getResource("/io/github/redstonemango/ttedit/image/bin_open.png").toExternalForm());
    public static final double MAX_LIBRARY_WIDTH = 470;
    public static final double MIN_LIBRARY_WIDTH = 10;

    private final ObservableList<Branch> branches;

    public ScriptElementEditor(ProjectElement element) {
        if (element.getType() != ProjectElement.Type.SCRIPT) throw new IllegalArgumentException("ProjectElement has to be a script");

        branches = FXCollections.observableArrayList();

        AnchorPane editorPane = new AnchorPane();
        ScrollPane editorScroll = new ScrollPane(editorPane);
        editorScroll.setPannable(true);
        ImageView deleteIcon = new ImageView(BIN_CLOSED);
        deleteIcon.setPreserveRatio(true);
        deleteIcon.setMouseTransparent(true);
        deleteIcon.setFitWidth(70);
        StackPane.setMargin(deleteIcon, new Insets(0, 20, 20, 0));
        StackPane editorArea = new StackPane(editorScroll, deleteIcon);
        editorArea.setAlignment(Pos.BOTTOM_RIGHT);
        HBox.setHgrow(editorArea, Priority.ALWAYS);

        double controlsAreaWidth = Project.getCurrentProject().getScriptBoxLibraryWidth();

        VBox controlsBox = new VBox(20);
        ScrollPane controlsScroll = new ScrollPane(controlsBox);
        TitledPane controlsPane = new TitledPane("Add Control", controlsScroll);
        controlsPane.prefHeightProperty().bind(heightProperty());
        controlsPane.prefWidthProperty().bind(controlsScroll.widthProperty().add(2));
        controlsPane.minWidthProperty().bind(controlsPane.prefWidthProperty());
        controlsPane.maxWidthProperty().bind(controlsPane.prefWidthProperty());
        controlsPane.setCollapsible(false);
        controlsScroll.setFitToWidth(true);
        controlsScroll.setPrefWidth(controlsAreaWidth);
        controlsScroll.setMinWidth(controlsAreaWidth);
        controlsScroll.setMaxWidth(controlsAreaWidth);
        controlsScroll.prefHeightProperty().bind(controlsPane.heightProperty().subtract(42));
        controlsBox.prefHeightProperty().bind(controlsScroll.heightProperty().subtract(15));
        controlsBox.setFillWidth(false);
        controlsBox.setPadding(new Insets(10, 2, 10, 2));
        applyDragResizing(controlsBox, controlsScroll);

        ScriptElementMeta meta = new ScriptElementMeta(editorPane, editorScroll, deleteIcon, branches,
                element, Project.getCurrentProject());
        controlsBox.getChildren().add(HeadScriptElement.createPreview(meta));
        controlsBox.getChildren().add(PScriptActionElement.createPreview(meta));
        controlsBox.getChildren().add(JScriptActionElement.createPreview(meta));
        controlsBox.getChildren().add(RegisterScriptActionElement.createPreview(meta));
        controlsBox.getChildren().add(NegScriptActionElement.createPreview(meta));
        controlsBox.getChildren().add(TScriptActionElement.createPreview(meta));

        getChildren().addAll(controlsPane, editorArea);

        double currX = 10; // Default padding
        Set<HeadScriptElement> heads = new HashSet<>();
        for (ScriptData data : element.getBranches()) {
            Tuple2<HeadScriptElement, Double> branchData =
                    branchFromData(data, meta);

            HeadScriptElement head = branchData.first;
            heads.add(head);

            branches.add(new Branch(head));

            // Add elements to scene graph
            AbstractScriptElement el = head;
            while (el != null) {
                editorPane.getChildren().add(el);
                el = el.getElementChild();
            }

            head.setLayoutX(currX);
            head.setLayoutY(10);

            currX += branchData.second + 40;
        }

        // Position the head's children
        heads.stream().findFirst().ifPresent(head ->
                UXUtilities.doOnceAvailable(head.sceneProperty(), _ -> Platform.runLater(() ->
                        heads.forEach(AbstractScriptElement::updateChildrenMove)))
        );
    }

    private static Tuple2<HeadScriptElement, Double> branchFromData(ScriptData data, ScriptElementMeta meta) {

        if (data.getType() != ScriptData.Type.HEAD) throw new IllegalArgumentException("ScriptData has to be of type HEAD");
        HeadScriptElement he = new HeadScriptElement(false, null, meta);
        he.loadFromData(data);

        double width = he.width();

        AbstractScriptElement lastElement = he;
        for (ScriptData action : data.getActions()) {
            AbstractScriptElement element = AbstractScriptElement.fromData(action, meta);
            lastElement.setElementChild(element);

            width = Math.max(width, element.width() + 20);
            lastElement = element;
        }

        return new Tuple2<>(he, width);
    }

    private static void applyDragResizing(VBox box, ScrollPane pane) {
        final double THRESHOLD = 10;
        AtomicBoolean isDragging = new AtomicBoolean();
        AtomicReference<Double> offset = new AtomicReference<>();

        // Register as filter to be able to consume the 'add block' click event when dragging
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            // Check is on right side
            if (e.getX() > pane.getWidth() - THRESHOLD
                    &&
                    e.getX() < pane.getWidth() + THRESHOLD) {
                isDragging.set(true);
                offset.set(e.getX() - pane.getWidth());
                e.consume();
            }
        });

        pane.setOnMouseMoved(e -> {
            if (e.getX() > pane.getWidth() - THRESHOLD
                    &&
                    e.getX() < pane.getWidth() + THRESHOLD) {
                pane.setCursor(Cursor.H_RESIZE);
            }
            else {
                if (!isDragging.get()) {
                    pane.setCursor(null);
                }
            }
        });
        box.setOnMouseDragged(e -> {
            if (isDragging.get()) { // Only if started at right corner
                double newWidth = e.getSceneX() - pane.localToScene(0, 0).getX();
                newWidth -= offset.get();
                newWidth = Math.clamp(newWidth, MIN_LIBRARY_WIDTH, MAX_LIBRARY_WIDTH);
                pane.setMinWidth(newWidth);
                pane.setPrefWidth(newWidth);
                pane.setMaxWidth(newWidth);
                Project.getCurrentProject().setScriptBoxLibraryWidth(newWidth);
            }
        });
        pane.setOnMouseReleased(_ -> {
            if (isDragging.get()) {
                isDragging.set(false);
                pane.setCursor(null);
                try {
                    ProjectIO.saveProjectGeneralConfig(Project.getCurrentProject());
                } catch (IOException _) {} // Fail silently
            }
        });
    }

    public ObservableList<Branch> getBranches() {
        return branches;
    }

    public record Branch(HeadScriptElement head, List<AbstractScriptActionElement> elements) {
        public Branch(HeadScriptElement head) {
            this(head, new ArrayList<>());
        }
    }

}
