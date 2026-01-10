package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.SoundSelectionNode;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PScriptActionElement extends AbstractScriptActionElement {

    public PScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent,
                                ScriptElementMeta meta) {
        super(preview, parent, false, meta);
    }

    public static PScriptActionElement createPreview(ScriptElementMeta meta) {
        return new PScriptActionElement(true, null,meta);
    }

    private ListProperty<Sound> sounds;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (sounds == null) sounds = new SimpleListProperty<>(FXCollections.observableArrayList());

        Label l = new Label("Play one of");
        applyColoring(l);
        l.setMouseTransparent(preview);
        SoundSelectionNode selectionView = new SoundSelectionNode(existingSounds, sounds, project);
        selectionView.setMouseTransparent(preview);
        selectionView.setFocusTraversable(false);
        applyColoring(selectionView);
        contentBox.getChildren().addAll(l, selectionView);
        sounds.addListener((ListChangeListener<? super Sound>) _ -> markChanged());
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.PLAY) throw new IllegalArgumentException("ScriptData has to be of type PLAY");
        sounds.setAll(data.getSounds().stream()
                .map(s -> Sound.fromString(s, existingSounds))
                .filter(Objects::nonNull)
                .toList()
        );
        markIsInBranch();
    }

    @Override
    public AbstractScriptElement createDefault(ScriptElementMeta meta) {
        return new PScriptActionElement(false, null, meta);
    }

    @Override
    public Color color() {
        return Color.CYAN;
    }

    @Override
    public ScriptData build() {
        ScriptData data = new ScriptData();
        data.setType(ScriptData.Type.PLAY);
        data.setSounds(sounds.stream()
                        .map(sound -> sound.soundFile().getName())
                        .distinct()
                        .toList());
        return data;
    }

    @Override
    public double width() {
        return 220;
    }

    @Override
    public double height() {
        return 20;
    }
}
