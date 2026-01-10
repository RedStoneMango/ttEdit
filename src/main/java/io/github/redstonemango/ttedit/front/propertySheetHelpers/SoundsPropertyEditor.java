package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.front.SoundSelectionNode;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.List;

public class SoundsPropertyEditor implements PropertyEditor<List<Sound>> {

    private final SoundSelectionNode selectionNode;
    private final ObservableList<Sound> selectedSounds;

    public SoundsPropertyEditor(PropertySheet.Item item, ObservableList<Sound> existingSounds, Project project) {

        @SuppressWarnings("unchecked")
        var l = (List<Sound>) item.getValue();
        selectedSounds = FXCollections.observableList(l);

        selectionNode = new SoundSelectionNode(existingSounds, selectedSounds, project);
        selectedSounds.addListener((ListChangeListener<? super Sound>) _ ->
                item.setValue(selectedSounds)
        );
    }

    @Override public Node getEditor() { return selectionNode; }
    @Override public List<Sound> getValue() { return selectedSounds; }
    @Override public void setValue(List<Sound> value) { /* We're listening to changes in existingSounds. Do not allow this way of editing */ }
}
