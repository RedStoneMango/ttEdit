package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.Sound;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;

public class SoundsPropertyEditor implements PropertyEditor<ObservableList<Sound>> {

    private final Button button;
    private ObservableList<Sound> sounds;
    private final PopOver popOver;
    private final Project project;

    @SuppressWarnings("unchecked")
    public SoundsPropertyEditor(SoundsPropertyItem item) {
        project = item.getProject();
        sounds = (ObservableList<Sound>) item.getValue();

        popOver = new PopOver();

        button = new Button(
                project != null
                        ? buildSoundsText()
                        : ""
        );
        button.setOnAction(_ -> {
            popOver.setContentNode(createPopOverContent());
            popOver.show(button);
        });
        button.setAlignment(Pos.CENTER_LEFT);

        popOver.setDetachable(false);
        popOver.setAnimated(true);
        popOver.setTitle("Sounds");
        popOver.setOnHidden(_ -> button.setText(buildSoundsText()));
    }

    private Node createPopOverContent() {
        return new Pane();
    }

    private String buildSoundsText() {
        if (sounds.isEmpty()) return "-- None --";

        StringBuilder builder = new StringBuilder();
        boolean firstCall = true;
        for (Sound sound : sounds) {
            if (!firstCall) builder.append(", ");
            firstCall = false;

            builder.append(sound.name());
        }
        return builder.toString();
    }

    @Override public Node getEditor() { return button; }
    @Override public ObservableList<Sound> getValue() { return sounds; }

    @Override
    public void setValue(ObservableList<Sound> value) {
        sounds = value;
        button.setText(buildSoundsText());
        popOver.setContentNode(createPopOverContent());
    }
}
