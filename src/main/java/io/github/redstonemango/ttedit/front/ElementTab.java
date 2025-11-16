package io.github.redstonemango.ttedit.front;

import io.github.redstonemango.ttedit.back.ProjectElement;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

public class ElementTab extends Tab {

    private final ProjectElement element;

    public ElementTab(ProjectElement element) {
        this.element = element;

        ImageView typeImageView = new ImageView(element.getType().buildImage());
        typeImageView.setPreserveRatio(true);
        typeImageView.setFitHeight(20);

        setText(element.getName());
        setGraphic(typeImageView);
    }

    public ProjectElement getElement() {
        return element;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof ElementTab tab)) return false;
        return tab.element == element;
    }
}
