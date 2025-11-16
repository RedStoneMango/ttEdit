package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.Nullable;

public class HeadScriptElement extends AbstractScriptElement {

    private int index;
    private int conditionCount = 0;
    private VBox conditionBox;

    public HeadScriptElement(boolean preview, Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                             @Nullable AbstractScriptElement parent,
                             ObservableList<ScriptEditor.Branch> branches) {
        super(preview, editorPane, editorScroll, deleteIcon, parent, true, branches);
    }

    public static HeadScriptElement createPreview(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                                  ObservableList<ScriptEditor.Branch> branches) {
        return new HeadScriptElement(true, editorPane, editorScroll, deleteIcon, null, branches);
    }

    @Override
    public void populate(HBox contentBox, boolean preview) {
        VBox content = new VBox(10);
        HBox.setMargin(content, new Insets(5, 0, 0, 0));
        content.setMouseTransparent(preview);


        ImageView idxUp = new ImageView(
                getClass().getResource("/io/github/redstonemango/ttedit/image/arrow_up.png").toExternalForm());
        idxUp.setOnMouseClicked(_ -> changeIndex(true));
        idxUp.setPreserveRatio(true);
        idxUp.setFitHeight(9);
        idxUp.setPickOnBounds(true);
        ImageView idxDown = new ImageView(
                getClass().getResource("/io/github/redstonemango/ttedit/image/arrow_down.png").toExternalForm());
        idxDown.setPreserveRatio(true);
        idxDown.setFitHeight(9);
        idxDown.setPickOnBounds(true);
        idxDown.setOnMouseClicked(_ -> changeIndex(false));
        VBox idxChangeBox = new VBox(idxUp, idxDown);

        Label idxLabel = new Label(String.valueOf(branches.size() + 1)); // Default if just added
        idxLabel.setFont(Font.font(null, FontWeight.NORMAL, 15));
        idxLabel.setBackground(new Background(new BackgroundFill(
                Color.MEDIUMPURPLE,
                new CornerRadii(6),
                new Insets(0, -5, 0, -5)
        )));
        HBox.setMargin(idxLabel, new Insets(3, 0, 0, 0));

        Label l1 = new Label("Branch");
        applyColoring(l1);
        HBox.setMargin(l1, new Insets(4, 0, 0, 0));

        HBox titleBox = new HBox(l1, idxLabel, idxChangeBox);
        titleBox.setSpacing(10);

        branches.addListener((ListChangeListener<? super ScriptEditor.Branch>) _ -> {
            if (preview) { // Always display highest+1 in preview
                idxLabel.setText(String.valueOf(branches.size() + 1));
            }
            else {
                int myIdx = retrieveIndex();
                idxLabel.setText(String.valueOf(myIdx + 1));
                idxUp.setOpacity(myIdx + 1 >= branches.size() ? 0.4 : 1);
                idxDown.setOpacity(myIdx <= 0 ? 0.4 : 1);
                index = myIdx; // Store for later computations
            }
        });

        MenuItem staticConditionItem = new MenuItem("+ Static Condition");
        MenuItem dynamicConditionItem = new MenuItem("+ Dynamic Condition");
        ContextMenu conditionCtxMenu = new ContextMenu(staticConditionItem, dynamicConditionItem);

        Button addConditionBtn = new Button("+ Condition");
        setFocusTraversable(false);
        applyColoring(addConditionBtn, false);
        addConditionBtn.setOnAction(_ -> {
            if (conditionCtxMenu.isShowing()) {
                conditionCtxMenu.hide();
                return;
            }
            Point2D pos = addConditionBtn.localToScreen(0, addConditionBtn.getHeight());
            conditionCtxMenu.show(addConditionBtn, pos.getX(), pos.getY());
        });
        conditionBox = new VBox(addConditionBtn);
        conditionBox.setSpacing(10);
        staticConditionItem.setOnAction(_ -> {
            conditionBox.getChildren().add(
                    conditionBox.getChildren().size() - 1,
                    new Condition(false, this)
            );
            updateShape();
            updateChildrenMove();
        });
        dynamicConditionItem.setOnAction(_ -> {
            conditionBox.getChildren().add(
                    conditionBox.getChildren().size() - 1,
                    new Condition(true, this)
            );
            updateShape();
            updateChildrenMove();
        });
        conditionBox.getChildren().addListener((ListChangeListener<? super Node>) _ ->
                conditionCount = conditionBox.getChildren().size() - 1);

        content.getChildren().addAll(titleBox, conditionBox);
        contentBox.getChildren().addAll(content);
    }

    private int retrieveIndex() {
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).head() == this) {
                return i;
            }
        }
        return -1;
    }

    private void changeIndex(boolean up) {
        if (branches == null || branches.isEmpty()) return;
        if (index < 0 || index >= branches.size()) return;

        int indexCopy = index; // Removal from list will trigger 'index' variable to update: Store it temporarily

        ScriptEditor.Branch myBranch = branches.get(index);
        branches.remove(index);

        if (up) {
            if (indexCopy < branches.size()) {
                indexCopy++;
            }
        } else {
            if (indexCopy > 0) {
                indexCopy--;
            }
        }

        branches.add(indexCopy, myBranch);
    }

    @Override
    public AbstractScriptElement createDefault(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                               @Nullable AbstractScriptElement parent,
                                               ObservableList<ScriptEditor.Branch> branches) {
        return new HeadScriptElement(false, editorPane, editorScroll, deleteIcon, null, branches);
    }

    @Override
    public Color color() {
        return Color.LIMEGREEN;
    }

    @Override
    public String build() {
        return "";
    }

    @Override
    public double width() {
        return conditionCount == 0 ? 80 : 365; // Smaller if there are no conditions
    }

    @Override
    public double height() {
        return 80 // Base height
                + conditionCount * 28 // Every condition is 28px high
                + Math.max(conditionCount - 1, 0) * 10; // 10px spacing between conditions
    }

    public static class Condition extends HBox {

        public Condition(boolean dynamic, HeadScriptElement owner) {
            setAlignment(Pos.CENTER);
            setSpacing(5);

            Label l1 = new Label("If");
            l1.setFocusTraversable(false);
            owner.applyColoring(l1);
            Label l2 = new Label("is");
            l2.setFocusTraversable(false);
            owner.applyColoring(l2);


            TextField fieldA = new TextField();
            fieldA.setPrefWidth(70);
            fieldA.setFocusTraversable(false);
            owner.applyColoring(fieldA);
            TextField fieldB = new TextField();
            fieldB.setPrefWidth(70);
            fieldB.setFocusTraversable(false);
            owner.applyColoring(fieldB);
            ComboBox<String> comparisonBox = new ComboBox<>();
            comparisonBox.setFocusTraversable(false);
            owner.applyColoring(comparisonBox);
            comparisonBox.getItems().addAll("less than", "less or equal to", "more than", "more or equal to",
                    "equal to", "not equal to");
            comparisonBox.getSelectionModel().select("equal to");


            ImageView remove = new ImageView(
                    getClass().getResource("/io/github/redstonemango/ttedit/image/dark_x.png").toExternalForm());
            remove.setPickOnBounds(true);
            remove.setPreserveRatio(true);
            remove.setFitWidth(20);
            UXUtilities.registerHoverAnimation(remove);
            HBox.setMargin(remove, new Insets(0, 0, 0, 5));
            remove.setOnMouseClicked(_ -> {
                owner.conditionBox.getChildren().remove(this);
                owner.updateShape();
                owner.updateChildrenMove();
            });


            getChildren().addAll(l1, fieldA, l2, comparisonBox, fieldB, remove);
        }
    }
}
