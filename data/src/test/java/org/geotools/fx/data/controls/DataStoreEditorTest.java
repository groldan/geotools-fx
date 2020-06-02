/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2020, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.fx.data.controls;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.geotools.arcsde.data.ArcSDEDataStoreFactory;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.property.PropertyDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.fx.data.model.DataStoreFactory;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class DataStoreEditorTest extends ApplicationTest {

    private DataStoreEditor chooser;

    public @Override void start(Stage stage) throws Exception {
        chooser = new DataStoreEditor();
        stage.setScene(new Scene(chooser, 640, 480));
        stage.show();
        /*
         * Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with
         * another window, the one in front of all the windows...
         */
        stage.toFront();
    }

    //    /* IMO, it is quite recommended to clear the ongoing events, in case of. */
    //    public @After void tearDown() throws TimeoutException {
    //        /* Close the window. It will be re-opened at the next test. */
    //        FxToolkit.hideStage();
    //        super.release(new KeyCode[] {});
    //        super.release(new MouseButton[] {});
    //    }

    public @Test void testAllRegisteredParamTypes() {
        ArrayList<DataAccessFactory> factories =
                Lists.newArrayList(DataAccessFinder.getAllDataStores());

        Set<Class<?>> paramTypes = new TreeSet<>((c1, c2) -> c1.getName().compareTo(c2.getName()));
        for (DataAccessFactory f : factories) {
            List<Param> params = Lists.newArrayList(f.getParametersInfo());
            List<Class<?>> types = Lists.transform(params, p -> p.getType());
            paramTypes.addAll(types);
        }
        paramTypes.forEach(System.err::println);
    }

    public @Test final void testPostgisFactory() throws InterruptedException {
        testFactory(new PostgisNGDataStoreFactory());
    }

    public @Test final void testShapefileFactory() throws InterruptedException {
        testFactory(new ShapefileDataStoreFactory());
    }

    public @Test final void testPropertiesFactory() throws InterruptedException {
        testFactory(new PropertyDataStoreFactory());
    }

    public @Test final void testArcsdeFactory() throws InterruptedException {
        testFactory(new ArcSDEDataStoreFactory());
    }

    public @Test final void testOracleFactory() throws InterruptedException {
        testFactory(new OracleNGDataStoreFactory());
    }

    public @Test final void testGeopkgFactory() throws InterruptedException {
        testFactory(new GeoPkgDataStoreFactory());
    }

    private void testFactory(DataAccessFactory dataAccess) throws InterruptedException {
        DataStoreFactory factory = new DataStoreFactory(dataAccess);
        chooser.setFactory(factory);
        Thread.sleep(500);
    }
}
