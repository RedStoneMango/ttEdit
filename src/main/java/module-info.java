module io.github.redstonemango.ttedit {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens io.github.redstonemango.ttedit to javafx.fxml;
    exports io.github.redstonemango.ttedit;
}