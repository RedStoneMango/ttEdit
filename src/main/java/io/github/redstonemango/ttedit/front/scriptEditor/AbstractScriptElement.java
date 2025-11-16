package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractScriptElement extends StackPane {

    private static final double ARC_RADIUS = 10;
    private static final double NOTCH_WIDTH = 20;
    private static final double NOTCH_OFFSET = 35;
    private static final double NOTCH_DEPTH = 5;
    private static final double SNAP_THRESHOLD = 30;

    private final Path shape = new Path();
    private final ImageView deleteIcon;
    private final boolean isHead;
    final boolean preview;
    final ObservableList<ScriptElementEditor.Branch> branches;

    private @Nullable Path highlightShape;

    private double dragOffsetX = -1;
    private double dragOffsetY = -1;

    private @Nullable AbstractScriptElement child;
    private @Nullable AbstractScriptElement parent;

    public AbstractScriptElement(boolean preview, Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                 @Nullable AbstractScriptElement parent, boolean isHead,
                                 ObservableList<ScriptElementEditor.Branch> branches) {
        this.parent = parent;
        this.deleteIcon = deleteIcon;
        this.isHead = isHead;
        this.preview = preview;
        this.branches = branches;

        HBox content = new HBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(10);
        populate(content, preview);
        getChildren().addAll(shape, content);
        shape.setFill(color());
        shape.setStroke(Color.GRAY);

        AtomicReference<Point2D> furthestEditorCorner = new AtomicReference<>(new Point2D(0, 0));
        AtomicReference<Point2D> nearestEditorCorner = new AtomicReference<>(new Point2D(0, 0));

        setOnMousePressed(e -> {
            if (preview) {
                Point2D scrollTopLeft = editorScroll.localToScene(0, 0);
                Point2D paneTopLeftVisual = editorPane.sceneToLocal(scrollTopLeft);
                AbstractScriptElement element = createDefault(editorPane, editorScroll, deleteIcon, null, branches);
                element.setLayoutX(paneTopLeftVisual.getX());
                element.setLayoutY(paneTopLeftVisual.getY());
                editorPane.getChildren().add(element);

                if (element instanceof HeadScriptElement head) {
                    branches.add(new ScriptElementEditor.Branch(head));
                }
                return;
            }
            dragOffsetX = e.getSceneX() - getLayoutX();
            dragOffsetY = e.getSceneY() - getLayoutY();

            // Move above the other elements
            List<AbstractScriptElement> children = resolveChildrenRecursively();
            editorPane.getChildren().remove(this);
            editorPane.getChildren().addLast(this);
            editorPane.getChildren().removeAll(children);
            editorPane.getChildren().addAll(children);

            if (hasElementParent()) {
                getElementParent().setElementChild(null);
            }

            editorScroll.setPannable(false);
            furthestEditorCorner.set(editorScroll.localToScene(editorScroll.getWidth(), editorScroll.getHeight()));
            nearestEditorCorner.set(editorScroll.localToScene(0, 0));

            setCursor(Cursor.MOVE);
        });

        setOnMouseDragged(e -> {
            if (preview) return;

            AbstractScriptElement lowest = lowestChild();
            AbstractSnapTarget snapTarget = findSnapTarget(this, lowest, editorPane);

            if (snapTarget != null) {
                showHighlightFor(snapTarget.target(), snapTarget.position(), editorPane);
            } else {
                clearHighlight(editorPane);
            }

            double newX = Math.max(e.getSceneX() - dragOffsetX, 0);
            double newY = Math.max(e.getSceneY() - dragOffsetY, 0);

            setLayoutX(newX);
            setLayoutY(newY);
            updateChildrenMove();

            Point2D furthestElementCorner = localToScene(getWidth(), getHeight());
            Point2D nearestElementCorner = localToScene(0, 0);

            if (furthestElementCorner.getX() > furthestEditorCorner.get().getX()) {
                getControlIntoViewHorizontally(this, editorScroll, editorPane);
            }
            else if (nearestElementCorner.getX() < nearestEditorCorner.get().getX()) {
                getControlIntoViewHorizontally(this, editorScroll, editorPane);
            }
            if (furthestElementCorner.getY() > furthestEditorCorner.get().getY()) {
                getControlIntoViewVertically(this, editorScroll, editorPane);
            }
            else if (nearestElementCorner.getY() < nearestEditorCorner.get().getY()) {
                getControlIntoViewVertically(this, editorScroll, editorPane);
            }

            deleteIcon.setImage(touchingDeleteIcon(e) ? ScriptElementEditor.BIN_OPEN : ScriptElementEditor.BIN_CLOSED);
        });

        setOnMouseReleased(e -> {
            if (preview) return;

            dragOffsetX = dragOffsetY = -1;
            clearHighlight(editorPane);

            AbstractScriptElement lowest = lowestChild();
            AbstractSnapTarget snapTarget = findSnapTarget(this, lowest, editorPane);

            if (snapTarget != null) {
                if (snapTarget.target() == this) return; // Safety
                if (snapTarget.position() == SnapPosition.BELOW)
                    doSnapFor(snapTarget.target(), snapTarget.position());
                else
                    lowest.doSnapFor(snapTarget.target(), snapTarget.position());
            }

            if (touchingDeleteIcon(e)) {
                List<AbstractScriptElement> children = resolveChildrenRecursively();
                editorPane.getChildren().removeAll(children);
                editorPane.getChildren().remove(this);

                if (this instanceof HeadScriptElement) {
                    branches.removeIf(b -> b.head() == this);
                }

                deleteIcon.setImage(ScriptElementEditor.BIN_CLOSED);
            }

            setCursor(Cursor.DEFAULT);
            editorScroll.setPannable(true);
        });

        content.widthProperty().addListener((_, _, _) -> updateShape());
        content.heightProperty().addListener((_, _, _) -> updateShape());
    }

    private void getControlIntoViewVertically(Region element, ScrollPane scrollPane, Pane parent) {
        double elementLayoutY = element.getBoundsInParent().getMinY();
        double elementHeight = element.getHeight();

        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double verticalScrollToTop = (elementLayoutY) / (parent.getHeight() - viewportHeight);
        double verticalScrollToBottom = (elementLayoutY - (viewportHeight - elementHeight)) / (parent.getHeight() - viewportHeight);
        double finalVerticalScrollValue = Math.abs(verticalScrollToTop - scrollPane.getVvalue()) > Math.abs(verticalScrollToBottom - scrollPane.getVvalue()) ? verticalScrollToBottom : verticalScrollToTop;

        scrollPane.setVvalue(finalVerticalScrollValue);
    }
    private void getControlIntoViewHorizontally(Region element, ScrollPane scrollPane, Pane parent) {
        double elementLayoutX = element.getBoundsInParent().getMinX();
        double elementWidth = element.getWidth();

        double viewportWidth = scrollPane.getViewportBounds().getWidth();

        double horizontalScrollToLeft = (elementLayoutX) / (parent.getWidth() - viewportWidth);
        double horizontalScrollToRight = (elementLayoutX - (viewportWidth - elementWidth)) / (parent.getWidth() - viewportWidth);
        double finalHorizontalScrollValue = Math.abs(horizontalScrollToLeft - scrollPane.getHvalue()) > Math.abs(horizontalScrollToRight - scrollPane.getHvalue()) ? horizontalScrollToRight : horizontalScrollToLeft;

        scrollPane.setHvalue(finalHorizontalScrollValue);
    }

    private boolean touchingDeleteIcon(MouseEvent event) {
        return deleteIcon
                .localToScene(deleteIcon.getBoundsInLocal())
                .contains(
                        new Point2D(
                                event.getSceneX(),
                                event.getSceneY()
                        )
                );
    }

    private AbstractSnapTarget findSnapTarget(AbstractScriptElement dragged, AbstractScriptElement lowest, Pane editorPane) {
        for (Node n : editorPane.getChildren()) {
            if (!(n instanceof AbstractScriptElement other) || other == dragged) continue;

            SnapPosition snap = dragged.getSnapPosition(other);
            if (snap == SnapPosition.BELOW && !other.hasElementChild() && !isHead) {
                return new AbstractSnapTarget(other, snap);
            }
        }

        for (Node n : editorPane.getChildren()) {
            if (!(n instanceof AbstractScriptElement other) || other == lowest) continue;

            SnapPosition snap = lowest.getSnapPosition(other);
            if (snap == SnapPosition.ABOVE && !other.isHead && !other.hasElementParent()) {
                return new AbstractSnapTarget(other, snap);
            }
        }
        return null;
    }

    void updateChildrenMove() {
        double childOffsetY = getHeight() - NOTCH_DEPTH - shape.getStrokeWidth();
        AbstractScriptElement curr = getElementChild();

        while (curr != null) {
            curr.setLayoutX(getLayoutX());
            curr.setLayoutY(getLayoutY() + childOffsetY);

            childOffsetY += curr.getHeight() - NOTCH_DEPTH - curr.shape.getStrokeWidth();
            curr = curr.getElementChild();
        }
    }

    private void updateParentsMove(double childX, double childY) {
        double parentOffsetY = 0;
        AbstractScriptElement curr = getElementParent();

        while (curr != null) {
            parentOffsetY += curr.getHeight() - NOTCH_DEPTH - curr.shape.getStrokeWidth();
            curr.setLayoutX(childX);
            curr.setLayoutY(childY - parentOffsetY);

            curr = curr.getElementParent();
        }
    }

    private AbstractScriptElement lowestChild() {
        AbstractScriptElement curr = this;
        while (curr != null) {
            if (curr.hasElementChild())
                curr = curr.getElementChild();
            else
                return curr;
        }
        return this;
    }

    private List<AbstractScriptElement> resolveChildrenRecursively() {
        AbstractScriptElement curr = getElementChild();
        List<AbstractScriptElement> children = new ArrayList<>();

        while (curr != null) {
            children.add(curr);
            curr = curr.getElementChild();
        }
        return children;
    }

    void updateShape() {
        double width = width() + 20;
        double height = height() + (isHead ? 10 : 25);

        shape.getElements().setAll(
                new MoveTo(ARC_RADIUS, 0),

                // top left corner
                new LineTo(NOTCH_OFFSET - NOTCH_WIDTH, 0),
                new LineTo(NOTCH_OFFSET - NOTCH_WIDTH + 4, NOTCH_DEPTH),
                new LineTo(NOTCH_OFFSET, NOTCH_DEPTH),
                new LineTo(NOTCH_OFFSET + 4, 0),

                // top right edge
                new LineTo(width - ARC_RADIUS, 0),

                // top-right rounded corner
                new ArcTo(ARC_RADIUS, ARC_RADIUS, 0, width, ARC_RADIUS, false, true),

                // right side
                new LineTo(width, height - ARC_RADIUS),

                // bottom-right rounded corner
                new ArcTo(ARC_RADIUS, ARC_RADIUS, 0, width - ARC_RADIUS, height, false, true),

                // bottom edge (with notch)
                new LineTo(NOTCH_OFFSET + 4, height),
                new LineTo(NOTCH_OFFSET, height + NOTCH_DEPTH),
                new LineTo(NOTCH_OFFSET - NOTCH_WIDTH + 4, height + NOTCH_DEPTH),
                new LineTo(NOTCH_OFFSET - NOTCH_WIDTH, height),

                // bottom-left corner
                new LineTo(ARC_RADIUS, height),
                new ArcTo(ARC_RADIUS, ARC_RADIUS, 0, 0, height - ARC_RADIUS, false, true),
                new LineTo(0, ARC_RADIUS),
                new ArcTo(ARC_RADIUS, ARC_RADIUS, 0, ARC_RADIUS, 0, false, true),

                new ClosePath()
        );
        if (isHead) shape.getElements().remove(2, 4); // Remove top notch

        // Already update dimensions, so they are up-to-date for following computations in the same app cycle
        setWidth(shape.getLayoutBounds().getWidth());
        setHeight(shape.getLayoutBounds().getHeight());
    }

    void applyColoring(Node node) {
        applyColoring(node, true);
    }

    void applyColoring(Node node, boolean textFill) {
        node.setStyle(
                "-fx-base: " + toRGBCode(color().darker()) + ";" +
                "-fx-accent: -fx-base;" +
                (textFill ? "-fx-text-fill: black;" : "")
        );
    }

    private SnapPosition getSnapPosition(AbstractScriptElement other) {
        if (Math.abs(other.getLayoutX() - getLayoutX()) >= SNAP_THRESHOLD)
            return SnapPosition.NONE;

        double belowDist = Math.abs(other.getLayoutY() + other.getHeight() - getLayoutY());
        if (belowDist < SNAP_THRESHOLD) return SnapPosition.BELOW;

        double aboveDist = Math.abs(other.getLayoutY() - getHeight() - getLayoutY());
        if (aboveDist < SNAP_THRESHOLD) return SnapPosition.ABOVE;

        return SnapPosition.NONE;
    }

    private void doSnapFor(AbstractScriptElement target, SnapPosition pos) {
        double posX = target.getLayoutX();
        double posY = (pos == SnapPosition.BELOW)
                ? target.getLayoutY() + target.getHeight() - NOTCH_DEPTH - shape.getStrokeWidth()
                : target.getLayoutY() - getHeight() + NOTCH_DEPTH + shape.getStrokeWidth();

        setLayoutX(posX);
        setLayoutY(posY);
        updateChildrenMove();
        updateParentsMove(posX, posY);

        if (pos == SnapPosition.BELOW) setElementParent(target);
        else if (pos == SnapPosition.ABOVE) setElementChild(target);
    }

    private void showHighlightFor(AbstractScriptElement target, SnapPosition pos, Pane editorPane) {
        double highlightX = target.getLayoutX();
        double highlightY = (pos == SnapPosition.BELOW)
                ? target.getLayoutY() + target.getHeight() - NOTCH_DEPTH - shape.getStrokeWidth()
                : target.getLayoutY() - getHeight() + NOTCH_DEPTH + shape.getStrokeWidth();

        // Avoid re-creating if position is same
        if (highlightShape != null &&
                highlightShape.getLayoutX() == highlightX &&
                highlightShape.getLayoutY() == highlightY) {
            return; // No change needed
        }

        clearHighlight(editorPane);

        highlightShape = new Path(shape.getElements());
        highlightShape.setFill(Color.DARKGRAY);
        highlightShape.setStroke(Color.TRANSPARENT);
        highlightShape.setOpacity(0.25);
        highlightShape.setLayoutX(highlightX);
        highlightShape.setLayoutY(highlightY);

        editorPane.getChildren().addFirst(highlightShape);
    }

    private void clearHighlight(Pane editorPane) {
        if (highlightShape != null) {
            editorPane.getChildren().remove(highlightShape);
            highlightShape = null;
        }
    }

    void setElementChild(@Nullable AbstractScriptElement newChild) {
        if (newChild == null) {
            if (child != null) child.parent = null;
            child = null;
        }
        else {
            if (child != null) throw new IllegalStateException("There already is a child for element " + this);
            child = newChild;
            newChild.parent = this;
        }
    }

    @Nullable AbstractScriptElement getElementChild() {
        return child;
    }

    boolean hasElementChild() {
        return child != null;
    }

    void setElementParent(@Nullable AbstractScriptElement newParent) {
        if (newParent == null) {
            if (parent != null) parent.setElementChild(null);
            parent = null;
        }
        else {
            if (parent != null) throw new IllegalStateException("There already is a parent for element " + this);
            parent = newParent;
            newParent.setElementChild(this);
        }
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    public static AbstractScriptElement fromData(ScriptData data, Pane editorPane, ScrollPane editorScroll,
                                                    ImageView deleteIcon, ObservableList<ScriptElementEditor.Branch> branches) {

        return switch (data.getType()) {
            case HEAD -> {
                HeadScriptElement he =
                        new HeadScriptElement(false, editorPane, editorScroll, deleteIcon, null, branches);
                he.loadFromData(data);
                yield he;
            }
            case PLAY -> {
                PScriptActionElement pe =
                        new PScriptActionElement(false, editorPane, editorScroll, deleteIcon, null, branches);
                pe.loadFromData(data);
                yield pe;
            }
            case JUMP -> {
                JScriptActionElement je =
                        new JScriptActionElement(false, editorPane, editorScroll, deleteIcon, null, branches);
                je.loadFromData(data);
                yield je;
            }
        };
    }

    @Nullable AbstractScriptElement getElementParent() {
        return parent;
    }

    boolean hasElementParent() {
        return parent != null;
    }

    public abstract void populate(HBox contentBox, boolean preview);

    public abstract AbstractScriptElement createDefault(Pane editorPane, ScrollPane editorScroll, ImageView deleteIcon,
                                                        @Nullable AbstractScriptElement parent,
                                                        ObservableList<ScriptElementEditor.Branch> branches);
    public abstract Color color();
    public abstract ScriptData build();
    abstract void loadFromData(ScriptData data);
    public abstract double width();
    public abstract double height();

    private enum SnapPosition { ABOVE, BELOW, NONE }
    private record AbstractSnapTarget(AbstractScriptElement target, SnapPosition position) {}
    private record GridKey(int x, int y) {}

}
