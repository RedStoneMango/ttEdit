package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import io.github.redstonemango.ttedit.back.Project;
import io.github.redstonemango.ttedit.back.projectElement.ScriptData;
import io.github.redstonemango.ttedit.back.projectElement.Sound;
import io.github.redstonemango.ttedit.front.UXUtilities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SoundsPropertyEditor implements PropertyEditor<Set<Sound>> {

    private final Button button;
    private Set<Sound> sounds;
    private final PopOver popOver;
    private final Project project;

    @SuppressWarnings("unchecked")
    public SoundsPropertyEditor(SoundsPropertyItem item) {
        project = item.getProject();
        sounds = (Set<Sound>) item.getValue();

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
    @Override public Set<Sound> getValue() { return sounds; }

    @Override
    public void setValue(Set<Sound> value) {
        sounds = value;
        button.setText(buildSoundsText());
        popOver.setContentNode(createPopOverContent());
    }
}
