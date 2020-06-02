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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.fx.data.model.DataStore;
import org.geotools.fx.data.model.Parameter;
import org.geotools.util.Converters;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/** */
@Accessors(fluent = true)
public class DataStoreLoader extends javafx.concurrent.Service<DataStore> {

    private final @Getter ListProperty<Parameter> parametersProperty =
            new SimpleListProperty<>(this, "parameters");

    @Override
    protected Task<DataStore> createTask() {
        final List<Parameter> parameters = new ArrayList<>(parametersProperty.get());
        return new Task<DataStore>() {
            protected @Override DataStore call() throws Exception {
                return loadDataStore(parameters);
            }
        };
    }

    private DataStore loadDataStore(@NonNull Iterable<Parameter> parameters) throws IOException {

        Map<String, Serializable> params = new HashMap<>();
        for (Parameter p : parameters) {
            String name = p.getDescriptor().getName();
            Object value = p.getValue();
            Serializable sval;
            if (value == null || value instanceof Serializable) {
                sval = (Serializable) value;
            } else {
                sval = Converters.convert(value, String.class);
            }
            params.put(name, sval);
        }
        DataAccess<FeatureType, Feature> dataAccess = DataAccessFinder.getDataStore(params);
        return new org.geotools.fx.data.model.DataStore(dataAccess);
    }
}
