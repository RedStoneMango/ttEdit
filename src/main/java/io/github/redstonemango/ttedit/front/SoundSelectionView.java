package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.back.Sound;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListSelectionView;

public class SoundSelectionView extends VBox {

    public SoundSelectionView(ObservableList<Sound> sounds, ObservableList<Sound> selectedSounds) {
        ListSelectionView<Sound> selectionView = new ListSelectionView<>();
        selectionView.setSourceItems(sounds);
        selectionView.setTargetItems(selectedSounds);
        selectionView.setSourceHeader(null);
        selectionView.setTargetHeader(null);
        selectionView.setCellFactory(UXUtilities.createSoundListCellFactory(true));
        getChildren().add(selectionView);
    }

}
