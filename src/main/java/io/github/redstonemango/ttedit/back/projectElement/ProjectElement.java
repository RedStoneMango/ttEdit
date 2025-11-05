package io.github.redstonemango.ttedit.back.projectElement;

import javafx.scene.image.Image;

public class ProjectElement {

    private final String name;
    private final Type type;

    public ProjectElement(String name, Type type) {
        this.name = name;
        this.type = type;
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
