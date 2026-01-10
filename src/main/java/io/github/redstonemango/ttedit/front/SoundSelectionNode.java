package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.Sound;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;

public class SoundSelectionNode extends Button {

    private final List<Sound> prevSounds = new ArrayList<>();
    private PopOver popOver;
    private ListSelectionView<Sound> popOverContent = null;
    private final ObservableList<Sound> sounds;
    private final ObservableList<Sound> selectedSounds;
    private final Project project;

    public SoundSelectionNode(ObservableList<Sound> sounds, ObservableList<Sound> selectedSounds,
                              Project project) {
        this.sounds = sounds;
        this.selectedSounds = selectedSounds;
        this.project = project;

        setPrefWidth(140);
        setAlignment(Pos.CENTER_LEFT);
        setOnAction(_ -> {
            prevSounds.clear();
            prevSounds.addAll(selectedSounds);
            popOverContent = createPopOverContent();
            popOver.setContentNode(popOverContent);
            popOver.show(this);
        });

        popOver = new PopOver();
        popOver.setDetachable(false);
        popOver.setAnimated(true);
        popOver.setTitle("Sound Selection");
        popOver.setOnHidden(_ -> {
            if (!prevSounds.equals(popOverContent.getTargetItems())) {
                selectedSounds.setAll(popOverContent.getTargetItems());
            }
        });
        sounds.addListener((ListChangeListener<? super Sound>) _ ->
            updateButtonText()
        );
        selectedSounds.addListener((ListChangeListener<? super Sound>) _ ->
            updateButtonText()
        );
    }

    private void updateButtonText() {
        StringBuilder builder = new StringBuilder();
        boolean firstRun = true;
        for (Sound sound : selectedSounds) {
            if (!firstRun) builder.append(", ");
            builder.append(sound.name());
            firstRun = false;
        }
        setText(builder.toString());
    }

    private ListSelectionView<Sound> createPopOverContent() {
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

        var sourceSounds = sounds.stream()
                .filter(s -> !selectedSounds.contains(s))
                .toList();
        ListSelectionView<Sound> selectionView = new ListSelectionView<>();
        selectionView.setSourceItems(FXCollections.observableArrayList(sourceSounds));
        selectionView.setTargetItems(FXCollections.observableArrayList(selectedSounds));
        selectionView.setCellFactory(UXUtilities.createSoundListCellFactory(true));
        selectionView.setSourceHeader(null);
        selectionView.setTargetHeader(null);
        selectionView.setSourceFooter(addSoundButton);
        return selectionView;
    }

}
