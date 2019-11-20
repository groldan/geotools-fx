module org.geotools.fx.demo {
    requires transitive org.geotools.fx.core;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;

    opens org.geotools.fx.demo to
            javafx.fxml;

    exports org.geotools.fx.demo;
}
