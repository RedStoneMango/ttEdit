package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.back.Sound;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListSelectionView;

public class SoundSelectionView extends VBox {

    private final ListSelectionView<Sound> selectionView;

    public SoundSelectionView(ObservableList<Sound> sounds) {
        selectionView = new ListSelectionView<>();
        selectionView.setSourceItems(sounds);
        selectionView.setSourceHeader(null);
        selectionView.setTargetHeader(null);
        selectionView.setCellFactory(UXUtilities.createSoundListCellFactory(true));
        getChildren().add(selectionView);
    }

    public ObservableList<Sound> getSelected() {
        return selectionView.getTargetItems();
    }

}
