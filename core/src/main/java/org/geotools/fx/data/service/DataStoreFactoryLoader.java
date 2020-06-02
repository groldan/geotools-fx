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
package org.geotools.fx.data.service;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.fx.data.model.DataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A {@link javafx.concurrent.Service} to load the available {@link
 * org.geotools.data.DataAccessFactory} SPI services as {@link
 * org.geotools.fx.data.model.DataStoreFactory} instances that provide JavaFX property bindings.
 *
 * <p>By default returns only factories for {@link SimpleFeatureType simple features} that report
 * themselves as {@link DataAccessFactory#isAvailable() available}.
 *
 * <p>Whether to report unavailable, complex, and/or simple feature data access factories can be
 * controlled through the provided properties.
 */
@Accessors(fluent = true)
public class DataStoreFactoryLoader extends Service<ObservableList<DataStoreFactory>> {

    private @Getter final BooleanProperty includeComplexFeaturesProperty =
            new SimpleBooleanProperty(this, "includeComplexFeatures", false);

    private @Getter final BooleanProperty includeSimpleFeaturesProperty =
            new SimpleBooleanProperty(this, "includeSimpleFeatures", true);

    private @Getter final BooleanProperty includeUnavailableProperty =
            new SimpleBooleanProperty(this, "includeUnavailable", false);

    protected @Override Task<ObservableList<DataStoreFactory>> createTask() {

        return new Task<ObservableList<DataStoreFactory>>() {

            final boolean simple = includeSimpleFeaturesProperty.get();
            final boolean complex = includeComplexFeaturesProperty.get();
            final boolean includeUnavailable = includeUnavailableProperty.get();

            protected @Override ObservableList<DataStoreFactory> call() throws Exception {
                Iterator<DataAccessFactory> factories;
                if (includeUnavailable) {
                    factories = getAllDataStores();
                } else {
                    factories = getAvailableDataStores();
                }
                Predicate<DataAccessFactory> filter =
                        factory ->
                                (simple && factory instanceof DataStoreFactorySpi)
                                        || (complex && !(factory instanceof DataStoreFactorySpi));

                ObservableList<DataStoreFactory> list =
                        StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(
                                                factories, Spliterator.DISTINCT),
                                        false)
                                .filter(filter)
                                .map(DataStoreFactory::new)
                                .collect(
                                        Collectors.toCollection(
                                                FXCollections::observableArrayList));
                return list;
            }
        };
    }

    protected Iterator<DataAccessFactory> getAllDataStores() {
        return DataAccessFinder.getAllDataStores();
    }

    protected Iterator<DataAccessFactory> getAvailableDataStores() {
        return DataAccessFinder.getAvailableDataStores();
    }

    /**
     * Indicates whether to include {@link DataAccessFactory} instances that are not instances of
     * {@link DataStoreFactorySpi} (i.e. do not explicitly declare to work only againt {@link
     * SimpleFeatureType})
     *
     * @return {@code true} if complex feature data access factories should be returned. Defaults to
     *     {@code false}
     */
    public boolean isIncludeComplexFeatures() {
        return includeComplexFeaturesProperty.get();
    }

    public void setIncludeComplexFeatures(boolean include) {
        includeComplexFeaturesProperty.set(include);
    }

    /**
     * @return {@code true} if simple feature data access factories should be returned. Defaults to
     *     {@code true}
     */
    public boolean isIncludeSimpleFeatures() {
        return includeSimpleFeaturesProperty.get();
    }

    public void setIncludeSimpleFeatures(boolean include) {
        includeSimpleFeaturesProperty.set(include);
    }

    /**
     * @return {@code true} if {@link DataAccessFactory}'s that are are not {@link
     *     DataAccessFactory#isAvailable() available} should be returned anyway. Defaults to {@code
     *     false}
     */
    public boolean isIncludeUnavailable() {
        return includeUnavailableProperty.get();
    }

    public void setIncludeUnavailable(boolean include) {
        includeUnavailableProperty.set(include);
    }
}
