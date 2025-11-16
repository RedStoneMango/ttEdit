package io.github.redstonemango.ttedit.back.projectElement;

public class ProjectLoadException extends Exception {
    public ProjectLoadException(String message) {
        super(message);
    }
    public ProjectLoadException(Throwable cause) {
        super(cause);
    }
    public ProjectLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
