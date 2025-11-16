package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.mangoutils.tuple.Tuple2;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.IElementEditable;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScriptElementEditor extends HBox implements IElementEditable {

    public static final Image BIN_CLOSED = new Image(
            ScriptElementEditor.class.getResource("/io/github/redstonemango/ttedit/image/bin_closed.png").toExternalForm());
    public static final Image BIN_OPEN = new Image(
            ScriptElementEditor.class.getResource("/io/github/redstonemango/ttedit/image/bin_open.png").toExternalForm());

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

        double currX = 10; // Default padding
        Set<HeadScriptElement> heads = new HashSet<>();
        for (ScriptData data : element.getBranches()) {
            Tuple2<HeadScriptElement, Double> branchData =
                    branchFromData(data, editorPane, editorScroll, deleteIcon, branches);

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
            UXUtilities.doOnceSceneLoads(head, _ -> Platform.runLater(() ->
                    heads.forEach(AbstractScriptElement::updateChildrenMove)))
        );
    }

    private static Tuple2<HeadScriptElement, Double> branchFromData(ScriptData data, Pane editorPane,
                                                                    ScrollPane editorScroll, ImageView deleteIcon,
                                                                    ObservableList<ScriptElementEditor.Branch> branches) {

        if (data.getType() != ScriptData.Type.HEAD) throw new IllegalArgumentException("ScriptData has to be of type HEAD");
        HeadScriptElement he = new HeadScriptElement(false, editorPane, editorScroll, deleteIcon, null, branches);
        he.loadFromData(data);

        double width = he.width();

        ScriptData curr = data.getChild();
        AbstractScriptElement lastElement = he;
        while (curr != null) {
            AbstractScriptElement child = AbstractScriptElement.fromData(curr, editorPane, editorScroll, deleteIcon, branches);
            lastElement.setElementChild(child);

            width = Math.max(width, child.width() + 20);
            lastElement = child;
            curr = curr.getChild();
        }

        return new Tuple2<>(he, width);
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
