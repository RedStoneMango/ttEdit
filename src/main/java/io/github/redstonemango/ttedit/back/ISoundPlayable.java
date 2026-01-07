package io.github.redstonemango.ttedit.back;

public interface ISoundPlayable {
    void play();
    void stopPlaying();
    void setOnPlaybackEnd(Runnable action);
}
