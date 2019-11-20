module org.geotools.fx.core {
    requires static lombok;
    requires java.logging;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive org.locationtech.jts;
    requires transitive org.geotools.opengis;
    requires transitive org.geotools.metadata;
    requires transitive org.geotools.referencing;
    requires transitive org.geotools.main;

    opens org.geotools.fx.data.model to
            javafx.fxml;

    exports org.geotools.fx.data.model;
}
