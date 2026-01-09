package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.Sound;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.PopOver;

public class SoundSelectionView extends VBox {

    public SoundSelectionView(ObservableList<Sound> sounds, ObservableList<Sound> selectedSounds,
                              Project project) {
        Button addSoundButton = new Button("Add Sound");
        addSoundButton.setOnAction(_ -> {
            PopupWindow popupWindow = getScene().getWindow() instanceof PopupWindow p ? p : null;

            Window window = popupWindow;
            Node ownerNode = null;
            Point2D ancPos = new Point2D(0, 0);
            Point2D pos = new Point2D(0, 0);
            if (popupWindow != null) {
                window = popupWindow.getOwnerWindow();
                ancPos = new Point2D(popupWindow.getAnchorX(), popupWindow.getAnchorY());
                pos = new Point2D(popupWindow.getX(), popupWindow.getY());
                ownerNode = popupWindow.getOwnerNode();
                popupWindow.hide();
            }

            var added = UXUtilities.showAddSoundUI(project, window);
            sounds.addAll(added);

            if (popupWindow != null) {
                if (ownerNode == null) popupWindow.show(window, ancPos.getX(), ancPos.getY());
                else popupWindow.show(ownerNode, ancPos.getX(), ancPos.getY());
                popupWindow.setX(pos.getX());
                popupWindow.setY(pos.getY());
            }
        });

        ListSelectionView<Sound> selectionView = new ListSelectionView<>();
        selectionView.setSourceItems(sounds);
        selectionView.setTargetItems(selectedSounds);
        selectionView.setCellFactory(UXUtilities.createSoundListCellFactory(true));
        selectionView.setSourceHeader(null);
        selectionView.setTargetHeader(null);
        selectionView.setSourceFooter(addSoundButton);
        getChildren().add(selectionView);
    }

}
