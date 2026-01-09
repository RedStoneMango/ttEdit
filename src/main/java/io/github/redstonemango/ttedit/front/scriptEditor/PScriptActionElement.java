package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.Sound;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.front.SoundSelectionView;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
    private final List<Sound> prevSounds = new ArrayList<>();
    private Button selectionButton;
    private PopOver popOver;
    private SoundSelectionView selectionView = null;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (sounds == null) sounds = new SimpleListProperty<>(FXCollections.observableArrayList());

        Label l = new Label("Play one of");
        applyColoring(l);
        l.setMouseTransparent(preview);
        selectionButton = new Button("");
        selectionButton.setPrefWidth(140);
        selectionButton.setMouseTransparent(preview);
        selectionButton.setFocusTraversable(false);
        selectionButton.setAlignment(Pos.CENTER_LEFT);
        selectionButton.setOnAction(_ -> {
            prevSounds.clear();
            prevSounds.addAll(sounds);
            var sourceSounds = existingSounds.stream()
                    .filter(s -> !sounds.contains(s))
                    .toList();
            selectionView = new SoundSelectionView(FXCollections.observableArrayList(sourceSounds), sounds, project);
            popOver.setContentNode(selectionView);
            popOver.show(selectionButton);
        });
        applyColoring(selectionButton);
        contentBox.getChildren().addAll(l, selectionButton);

        popOver = new PopOver();
        popOver.setDetachable(false);
        popOver.setAnimated(true);
        popOver.setTitle("Sound Selection");
        popOver.setOnHidden(_ -> {
            updateButtonText();
            if (!prevSounds.equals(sounds)) markChanged();
        });
    }

    private void updateButtonText() {
        StringBuilder builder = new StringBuilder();
        boolean firstRun = true;
        for (Sound sound : sounds) {
            if (!firstRun) builder.append(", ");
            builder.append(sound.name());
            firstRun = false;
        }
        selectionButton.setText(builder.toString());
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.PLAY) throw new IllegalArgumentException("ScriptData has to be of type PLAY");
        sounds.setAll(data.getSounds().stream()
                .map(s -> Sound.fromStringExact(s, existingSounds))
                .filter(Objects::nonNull)
                .toList()
        );
        updateButtonText();
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
