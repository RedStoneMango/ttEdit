package io.github.redstonemango.ttedit.back.projectElement;

import java.io.File;

public record Sound(String name, File soundFile) {
    public Sound(File soundFile) {
        this(soundFile.getName().substring(0, soundFile.getName().lastIndexOf(".")), soundFile);
    }
}
