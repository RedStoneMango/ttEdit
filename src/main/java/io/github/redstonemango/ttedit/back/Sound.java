package io.github.redstonemango.ttedit.back;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Sound other)) return false;
        return soundFile.getAbsoluteFile().equals(other.soundFile.getAbsoluteFile());
    }

    public static @Nullable Sound fromString(String string, List<Sound> existingSounds) {
        return existingSounds.stream()
                .filter(sound -> sound.soundFile.getName().equals(string))
                .findFirst()
                .orElse(null);
    }

    public static @NotNull String toString(Sound sound) {
        return sound.soundFile.getName();
    }
}
