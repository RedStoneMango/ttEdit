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
        ListSelectionView<Sound> selectionView = new ListSelectionView<>();

        Button addSoundButton = new Button("Add Sound");
        addSoundButton.setOnAction(_ -> {
            Window window = popOver.getOwnerWindow();
            Node ownerNode = popOver.getOwnerNode();
            Point2D ancPos = new Point2D(popOver.getAnchorX(), popOver.getAnchorY());
            Point2D pos = new Point2D(popOver.getX(), popOver.getY());
            popOver.hide();

            var added = UXUtilities.showAddSoundUI(project, window);
            selectionView.getSourceItems().addAll(added);

            if (ownerNode == null) popOver.show(window, ancPos.getX(), ancPos.getY());
            else popOver.show(ownerNode, ancPos.getX(), ancPos.getY());
            popOver.setX(pos.getX());
            popOver.setY(pos.getY());
        });

        var sourceSounds = sounds.stream()
                .filter(s -> !selectedSounds.contains(s))
                .toList();
        selectionView.setSourceItems(FXCollections.observableArrayList(sourceSounds));
        selectionView.setTargetItems(FXCollections.observableArrayList(selectedSounds));
        selectionView.setCellFactory(UXUtilities.createSoundListCellFactory(true));
        selectionView.setSourceHeader(null);
        selectionView.setTargetHeader(null);
        selectionView.setSourceFooter(addSoundButton);
        return selectionView;
    }

}
