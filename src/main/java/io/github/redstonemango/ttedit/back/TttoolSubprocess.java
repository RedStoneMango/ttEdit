package io.github.redstonemango.ttedit.back;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;

public class TttoolSubprocess {

    public static @Nullable File findTttool() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isEmpty()) {
            return null;
        }

        String pathSeparator = File.pathSeparator;
        String[] paths = pathEnv.split(pathSeparator);

        String[] extensions = {""};
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        if (windows) {
            String pathext = System.getenv("PATHEXT");
            if (pathext != null) {
                extensions = pathext.toLowerCase().split(";");
            }
        }

        for (String dir : paths) {
            for (String ext : extensions) {
                File file = new File(dir, "tttool" + ext);
                if (file.isFile() && file.canExecute()) {
                    return file;
                }
            }
        }

        return null;
    }

}
