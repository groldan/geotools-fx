package org.geotools.fx.demo;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.fx.data.model.Parameter;
import org.geotools.fx.data.model.ParameterDescriptor;

/** JavaFX App */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        ParameterDescriptor ds = new ParameterDescriptor(new Param("TEST"));
        Parameter param = new Parameter(ds, "value");
        System.err.println(param);

        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
