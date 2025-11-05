package io.github.redstonemango.ttedit;

import io.github.redstonemango.mangoutils.LogManager;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Application;

import java.io.File;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class Launcher {

    public static final File APP_HOME = OperatingSystem.loadCurrentOS().createAppConfigDir("ttEdit");
    public static final File PROJECTS_HOME = new File(APP_HOME, "projects");

    public static void main(String[] args) {
        setupLogManagement();
        PROJECTS_HOME.mkdirs(); // Automatically makes APP_HOME

        Application.launch(TtEdit.class, args);
    }

    private static void setupLogManagement() {
        LogManager.logDir(Paths.get(APP_HOME.getAbsolutePath(), "logs"));
        LogManager.logFileHeaderFunction(date -> """
                This is a log file for the ttEdit application.
                Inside this log file, all outputs the app logged at $DATE$ are logged.
                This file will compress itself to a .gz archive in one day.
                7 Days after compression, the archive will delete itself to save storage
                """.replace("$DATE$", DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date)));
        LogManager.logFileNameFunction(date -> "ttedit_" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date));
        LogManager.disableLogFiles(true);
        LogManager.start();
        System.out.println("Initialized log manager using the default configuration");
    }
}
