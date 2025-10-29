package io.github.redstonemango.ttedit;

import io.github.redstonemango.mangoutils.LogManager;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Application;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class Launcher {

    public static final String APP_HOME = OperatingSystem.loadCurrentOS().createAppConfigDir("ttEdit").getAbsolutePath();

    public static void main(String[] args) {
        setupLogManagement();
        Application.launch(ttEdit.class, args);
    }

    private static void setupLogManagement() {
        LogManager.logDir(Paths.get(APP_HOME, "logs"));
        LogManager.logFileHeaderFunction(date -> """
                This is a log file for the ttEdit application.
                Inside this log file, all outputs the app logged at $DATE$ are logged.
                This file will compress itself to a .gz archive in one day.
                7 Days after compression, the archive will delete itself to save storage
                """.replace("$DATE$", DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date)));
        LogManager.logFileNameFunction(date -> "ttedit_" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date));
        LogManager.disableLogFiles(true);
        LogManager.start();
        System.out.println("Initialized log manager using the default configurations");
    }
}
