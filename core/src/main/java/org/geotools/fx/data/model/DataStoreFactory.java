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
package org.geotools.fx.data.model;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFactory.Param;

/** JavaFX Bean adapter for {@link DataAccessFactory} */
@ToString(onlyExplicitlyIncluded = true)
@Accessors(fluent = true)
public class DataStoreFactory {

    private final @Getter ReadOnlyObjectProperty<DataAccessFactory> factoryProperty;
    private final @Getter ReadOnlyStringProperty displayNameProperty;
    private final @Getter ReadOnlyStringProperty descriptionProperty;
    private final @Getter ReadOnlyBooleanProperty availableProperty;
    private final @Getter ReadOnlyListProperty<ParameterDescriptor> parameterDescriptorsProperty;

    public DataStoreFactory(@NonNull DataAccessFactory factory) {
        factoryProperty = new SimpleObjectProperty<>(this, "factory", factory);
        displayNameProperty =
                new SimpleStringProperty(this, "displayName", factory.getDisplayName());
        descriptionProperty =
                new SimpleStringProperty(this, "description", factory.getDescription());

        Param[] parameters = factory.getParametersInfo();
        ObservableList<ParameterDescriptor> params = FXCollections.observableArrayList();
        for (Param p : parameters) {
            params.add(new ParameterDescriptor(p));
        }
        parameterDescriptorsProperty =
                new SimpleListProperty<>(this, "parameterDescriptors", params);

        // derived, lazy
        try {
            availableProperty =
                    ReadOnlyJavaBeanBooleanPropertyBuilder.create()
                            .bean(factory)
                            .name("available")
                            .build();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public DataAccessFactory getFactory() {
        return factoryProperty.get();
    }

    @ToString.Include(name = "displayName")
    public String getDisplayName() {
        return displayNameProperty.get();
    }

    @ToString.Include(name = "description")
    public String getDescription() {
        return descriptionProperty.get();
    }

    @ToString.Include(name = "available")
    public boolean isAvailable() {
        return availableProperty.get();
    }

    public ObservableList<ParameterDescriptor> getParameterDescriptors() {
        return parameterDescriptorsProperty;
    }
}
