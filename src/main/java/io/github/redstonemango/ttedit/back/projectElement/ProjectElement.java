package io.github.redstonemango.ttedit.back.projectElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;

public class ProjectElement {

    @JsonIgnore private String name;
    private Type type;

    public ProjectElement() {}

    public ProjectElement(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void initializeFields(String filename) {
        name = filename.substring(0, nthLastIndexOf(2, ".", filename));
        if (type == null); // TODO: Implement detection based on which fields exist in this object. Then, make type a @JsonIgnore
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

    public enum Type {
        SCRIPT, PAGE;

        public Image buildImage() {
            return new Image(getClass().getResource("/io/github/redstonemango/ttedit/image/" +
                    this.toString().toLowerCase() + ".png").toExternalForm());
        }
    }
}
