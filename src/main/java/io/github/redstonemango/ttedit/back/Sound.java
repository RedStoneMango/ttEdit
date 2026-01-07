package io.github.redstonemango.ttedit.back;

import java.io.File;

public record Sound(String name, File soundFile) {
    public Sound(File soundFile) {
        this(
                soundFile.getName().contains(".")
                ?
                soundFile.getName().substring(0, soundFile.getName().lastIndexOf("."))
                :
                soundFile.getName(),
            soundFile
        );
    }

    public ISoundPlayable getPlayer() {
        if (soundFile.getName().endsWith(".mp3")) {
            return new Mp3SoundPlayer(this);
        }
        throw new IllegalStateException("The sound support MP3 players only");
    }
}
