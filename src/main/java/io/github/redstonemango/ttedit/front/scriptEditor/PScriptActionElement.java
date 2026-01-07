package io.github.redstonemango.ttedit.front.scriptEditor;

import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.jetbrains.annotations.Nullable;

public class PScriptActionElement extends AbstractScriptActionElement {

    public PScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent,
                                ScriptElementMeta meta) {
        super(preview, parent, false, meta);
    }

    public static PScriptActionElement createPreview(ScriptElementMeta meta) {
        return new PScriptActionElement(true, null,meta);
    }

    private SetProperty<String> sounds;
    private Button selectionButton;
    private PopOver popOver;

    @Override
    public void populate(HBox contentBox, boolean preview) {
        if (sounds == null) sounds = new SimpleSetProperty<>(FXCollections.observableSet());

        Label l = new Label("Play one of");
        applyColoring(l);
        l.setMouseTransparent(preview);
        selectionButton = new Button("");
        selectionButton.setPrefWidth(140);
        selectionButton.setMouseTransparent(preview);
        selectionButton.setFocusTraversable(false);
        selectionButton.setAlignment(Pos.CENTER_LEFT);
        selectionButton.setOnAction(_ -> {
            popOver.setContentNode(new Pane()); // TODO: Embed selection view
            popOver.show(selectionButton);
        });
        sounds.addListener((_, _, _) ->
                updateButtonText());
        applyColoring(selectionButton);
        contentBox.getChildren().addAll(l, selectionButton);

        popOver = new PopOver();
        popOver.setDetachable(false);
        popOver.setAnimated(true);
        popOver.setTitle("Sound Selection");
        popOver.setOnHidden(_ -> updateButtonText());
    }

    private void updateButtonText() {
        selectionButton.setText(sounds.get().toString());
    }

    @Override
    void loadFromData(ScriptData data) {
        if (data.getType() != ScriptData.Type.PLAY) throw new IllegalArgumentException("ScriptData has to be of type PLAY");
        sounds.clear();
        sounds.addAll(data.getSounds());
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
        data.setSounds(sounds.get());
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
