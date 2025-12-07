package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.BranchCondition;
import io.github.redstonemango.ttedit.back.projectElement.ProjectElement;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class HeadScriptElement extends AbstractScriptElement {

    private int index;
    private int conditionCount = 0;
    private VBox conditionBox;

    public HeadScriptElement(boolean preview, @Nullable AbstractScriptElement parent,
                             ScriptElementMeta meta) {
        super(preview, parent, true, meta);
    }

    public static HeadScriptElement createPreview(ScriptElementMeta meta) {
        return new HeadScriptElement(true, null, meta);
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

        branches.addListener((ListChangeListener<? super ScriptElementEditor.Branch>) _ -> {
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

        Button addConditionBtn = new Button("+ Condition");
        setFocusTraversable(false);
        applyColoring(addConditionBtn, false);
        addConditionBtn.setOnAction(_ -> {
            addCondition(BranchCondition.Comparison.EQUAL, "", "");
        });
        conditionBox = new VBox(addConditionBtn);
        conditionBox.setSpacing(10);
        conditionBox.getChildren().addListener((ListChangeListener<? super Node>) _ -> {
            conditionCount = conditionBox.getChildren().size() - 1;
            markChanged();
        });

        content.getChildren().addAll(titleBox, conditionBox);
        contentBox.getChildren().addAll(content);
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.HEAD) throw new IllegalArgumentException("ScriptData has to be of type HEAD");
        data.getConditions().forEach(condition ->
            addCondition(condition.getComparison(), condition.getArgA(), condition.getArgB())
        );
        markIsInBranch();
    }

    private void addCondition(BranchCondition.Comparison comparison, String argA, String argB) {
        conditionBox.getChildren().add(
                conditionBox.getChildren().size() - 1,
                new Condition(comparison, argA, argB, this)
        );
        updateShape();
        updateChildrenMove();
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

        ScriptElementEditor.Branch myBranch = branches.get(index);
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
        markChanged();
    }

    @Override
    public AbstractScriptElement createDefault(ScriptElementMeta meta) {
        return new HeadScriptElement(false, null, meta);
    }

    @Override
    public Color color() {
        return Color.LIMEGREEN;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.HEAD);
        data.setConditions(new ArrayList<>());
        data.setActions(new ArrayList<>());
        for (Node child : conditionBox.getChildren()) {
            if (child instanceof Condition condition) {
                data.getConditions().add(condition.build());
            }
        }
        AbstractScriptElement curr = getElementChild();
        while (curr != null) {
            data.getActions().add(curr.build());
            curr = curr.getElementChild();
        }
        return data;
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

        private BranchCondition.Comparison comparison = BranchCondition.Comparison.EQUAL;
        private String argA = "";
        private String argB = "";

        public Condition(BranchCondition.Comparison defComparison, String defArgA,
                         String defArgB, HeadScriptElement owner) {
            setAlignment(Pos.CENTER);
            setSpacing(5);

            Label l1 = new Label("If");
            l1.setFocusTraversable(false);
            owner.applyColoring(l1);
            Label l2 = new Label("is");
            l2.setFocusTraversable(false);
            owner.applyColoring(l2);


            TextField fieldA = new TextField(defArgA);
            fieldA.setPrefWidth(70);
            fieldA.setFocusTraversable(false);
            fieldA.textProperty().addListener((_, _, val) -> {
                argA = val;
                owner.markChanged();
            });
            argA = defArgA;
            UXUtilities.applyRegisterCompletion(fieldA, owner.element, Project.getCurrentProject().getRegisterIndexUnifier());
            owner.applyColoring(fieldA);

            TextField fieldB = new TextField(defArgB);
            fieldB.setPrefWidth(70);
            fieldB.setFocusTraversable(false);
            fieldB.textProperty().addListener((_, _, val) -> {
                argB = val;
                owner.markChanged();
            });
            argB = defArgB;
            UXUtilities.applyRegisterCompletion(fieldB, owner.element, Project.getCurrentProject().getRegisterIndexUnifier());
            owner.applyColoring(fieldB);

            ComboBox<BranchCondition.Comparison> comparisonBox = new ComboBox<>();
            comparisonBox.setFocusTraversable(false);
            owner.applyColoring(comparisonBox);
            comparisonBox.getItems().addAll(BranchCondition.Comparison.values());
            UXUtilities.applyComparisonComboBoxCellFactory(comparisonBox);
            comparisonBox.getSelectionModel().select(defComparison);
            comparisonBox.getSelectionModel().selectedItemProperty()
                    .addListener((_, _, val) -> {
                        comparison = val;
                        owner.markChanged();
                    });
            comparison = defComparison;


            ImageView remove = new ImageView(
                    getClass().getResource("/io/github/redstonemango/ttedit/image/dark_x.png").toExternalForm());
            remove.setPickOnBounds(true);
            remove.setPreserveRatio(true);
            remove.setFitWidth(20);
            remove.setCursor(Cursor.HAND);
            UXUtilities.registerHoverAnimation(remove);
            HBox.setMargin(remove, new Insets(0, 0, 0, 5));
            remove.setOnMouseClicked(_ -> {
                owner.conditionBox.getChildren().remove(this);
                owner.updateShape();
                owner.updateChildrenMove();
            });


            getChildren().addAll(l1, fieldA, l2, comparisonBox, fieldB, remove);
        }

        public BranchCondition build() {
            BranchCondition condition = new BranchCondition();
            condition.setComparison(comparison);
            condition.setArgA(argA);
            condition.setArgB(argB);
            return condition;
        }
    }
}
