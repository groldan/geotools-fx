/*
 * GeoTools - The Open Source Java GIS Toolkit http://geotools.org
 *
 * (C) 2019, Open Source Geospatial Foundation (OSGeo) (C)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.geotools.fx.data.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.property.PropertyDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.fx.data.model.DataStoreFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public class DataStoreFactoryLoaderTest {

    private DataStoreFactoryLoader service;

    private TestDataAccessFactory testDataAccessFactory;
    private TestDataStoreFactory testDataStoreFactory;

    public static @BeforeClass void beforeClass() {
        Platform.startup(() -> {});
    }

    public static @AfterClass void afterClass() {
        Platform.exit();
    }

    public @Before void before() {
        testDataAccessFactory = new TestDataAccessFactory();
        testDataStoreFactory = new TestDataStoreFactory();

        service =
                new DataStoreFactoryLoader() {
                    protected @Override Iterator<DataAccessFactory> getAllDataStores() {
                        return append(
                                super.getAllDataStores(),
                                Arrays.asList(
                                        new TestDataAccessFactory(), new TestDataStoreFactory()));
                    }

                    protected @Override Iterator<DataAccessFactory> getAvailableDataStores() {
                        List<DataAccessFactory> extra = new ArrayList<>();
                        if (testDataAccessFactory.available) {
                            extra.add(testDataAccessFactory);
                        }
                        if (testDataStoreFactory.available) {
                            extra.add(testDataStoreFactory);
                        }
                        return append(super.getAvailableDataStores(), extra);
                    }

                    private Iterator<DataAccessFactory> append(
                            Iterator<DataAccessFactory> real, List<DataAccessFactory> additional) {
                        List<DataAccessFactory> l = new ArrayList<>(additional);
                        real.forEachRemaining(l::add);
                        return l.iterator();
                    }
                };
    }

    public @Test void defaultLoad() {
        List<DataAccessFactory> factories = load();

        assertTrue(factories.size() >= 2);
        assertTrue(factories.stream().anyMatch(ShapefileDataStoreFactory.class::isInstance));
        assertTrue(factories.stream().anyMatch(PropertyDataStoreFactory.class::isInstance));
        assertTrue(factories.stream().anyMatch(TestDataStoreFactory.class::isInstance));
        assertFalse(factories.stream().anyMatch(f -> !DataStoreFactorySpi.class.isInstance(f)));
    }

    public @Test void includeComplexFeaturesProperty() {
        List<DataAccessFactory> factories;

        service.setIncludeComplexFeatures(true);
        factories = load();
        assertTrue(factories.contains(testDataAccessFactory));

        service.setIncludeComplexFeatures(false);
        factories = load();
        assertFalse(factories.contains(testDataAccessFactory));
    }

    public @Test void includeSimpleFeaturesProperty() {
        List<DataAccessFactory> factories;
        service.setIncludeComplexFeatures(true);
        service.setIncludeSimpleFeatures(true);

        factories = load();
        assertTrue(factories.contains(testDataStoreFactory));
        assertTrue(factories.contains(testDataAccessFactory));

        service.setIncludeSimpleFeatures(false);
        factories = load();
        assertTrue(factories.contains(testDataAccessFactory));
        assertFalse(factories.contains(testDataStoreFactory));
    }

    public @Test void includeUnavailableProperty() {
        List<DataAccessFactory> factories;
        service.setIncludeComplexFeatures(true);
        service.setIncludeSimpleFeatures(true);

        testDataAccessFactory.available = true;
        testDataStoreFactory.available = true;

        factories = load();
        assertTrue(factories.contains(testDataAccessFactory));
        assertTrue(factories.contains(testDataStoreFactory));

        testDataAccessFactory.available = false;
        testDataStoreFactory.available = true;

        factories = load();
        assertFalse(factories.contains(testDataAccessFactory));
        assertTrue(factories.contains(testDataStoreFactory));

        testDataAccessFactory.available = true;
        testDataStoreFactory.available = false;

        factories = load();
        assertTrue(factories.contains(testDataAccessFactory));
        assertFalse(factories.contains(testDataStoreFactory));
    }

    private List<DataAccessFactory> load() {
        ObservableList<DataStoreFactory> value = FXCollections.observableArrayList();

        EventHandler<WorkerStateEvent> handler =
                new EventHandler<WorkerStateEvent>() {
                    public @Override void handle(WorkerStateEvent event) {
                        value.addAll(service.getValue());
                    }
                };
        Platform.runLater(
                () -> {
                    service.setOnSucceeded(handler);
                    service.restart();
                });
        Awaitility.await().atMost(Durations.TWO_SECONDS).until(() -> !value.isEmpty());

        List<DataAccessFactory> factories =
                value.stream().map(DataStoreFactory::getFactory).collect(Collectors.toList());
        return factories;
    }

    public static class TestDataAccessFactory implements DataAccessFactory {

        boolean available = true;

        public @Override DataAccess<? extends FeatureType, ? extends Feature> createDataStore(
                Map<String, Serializable> params) throws IOException {
            return null;
        }

        public @Override String getDisplayName() {
            return getClass().getSimpleName();
        }

        public @Override String getDescription() {
            return getClass().getSimpleName();
        }

        public @Override Param[] getParametersInfo() {
            return new Param[0];
        }

        public @Override boolean isAvailable() {
            return available;
        }
    }

    public static class TestDataStoreFactory extends TestDataAccessFactory
            implements DataStoreFactorySpi {

        public @Override DataStore createDataStore(Map<String, Serializable> params)
                throws IOException {
            return null;
        }

        public @Override DataStore createNewDataStore(Map<String, Serializable> params)
                throws IOException {
            return null;
        }
    }
}
