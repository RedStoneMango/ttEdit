package io.github.redstonemango.ttedit.back.projectElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectElement {

    @JsonIgnore private String name;
    @JsonIgnore private Type type;
    // SCRIPT ELEMENT
    private @Nullable List<ScriptData> branches;

    public ProjectElement() {}

    public ProjectElement(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void initializeFields(String filename) throws ProjectLoadException {
        name = filename.substring(0, nthLastIndexOf(2, ".", filename));
        this.type = Type.fromFileName(filename);
        if (type == Type.SCRIPT) {
            if (branches == null) {
                branches = new ArrayList<>();
            }
            for (ScriptData data : branches) {
                data.initializeFields();
            }
        }
    }

    private static int nthLastIndexOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<ScriptData> getBranches() {
        return branches;
    }

    public enum Type {
        SCRIPT, PAGE;

        public Image buildImage() {
            return new Image(getClass().getResource("/io/github/redstonemango/ttedit/image/" +
                    this.toString().toLowerCase() + ".png").toExternalForm());
        }

        public String fileSuffix() {
            return "." + toString().toLowerCase() + ".json";
        }

        public static Type fromFileName(String filename) {
            return filename.endsWith(SCRIPT.fileSuffix()) ? SCRIPT : PAGE;
        }
    }
}
