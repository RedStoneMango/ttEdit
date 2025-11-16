package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.front.IElementEditable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class ScriptElementEditor extends HBox implements IElementEditable {

    public static final Image BIN_CLOSED = new Image(
            ScriptElementEditor.class.getResource("/io/github/redstonemango/ttedit/image/bin_closed.png").toExternalForm());
    public static final Image BIN_OPEN = new Image(
            ScriptElementEditor.class.getResource("/io/github/redstonemango/ttedit/image/bin_open.png").toExternalForm());

    private final ObservableList<Branch> branches;

    public ScriptElementEditor() {
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

        VBox controlsBox = new VBox(20);
        TitledPane controlsPane = new TitledPane("Add Control", new ScrollPane(controlsBox));
        controlsPane.setPrefWidth(250);
        controlsPane.setMinWidth(250);
        controlsPane.setMaxWidth(250);
        controlsPane.prefHeightProperty().bind(heightProperty());
        controlsPane.setCollapsible(false);
        controlsBox.setSpacing(20);
        controlsBox.setFillWidth(false);
        controlsBox.setPadding(new Insets(10, 2, 10, 2));
        controlsBox.getChildren().add(HeadScriptElement.createPreview(editorPane, editorScroll, deleteIcon, branches));
        controlsBox.getChildren().add(PScriptActionElement.createPreview(editorPane, editorScroll, deleteIcon, branches));
        controlsBox.getChildren().add(JScriptActionElement.createPreview(editorPane, editorScroll, deleteIcon, branches));

        getChildren().addAll(controlsPane, editorArea);
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
