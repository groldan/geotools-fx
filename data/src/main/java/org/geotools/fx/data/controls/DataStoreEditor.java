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
package org.geotools.fx.data.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.geotools.fx.data.model.DataStoreFactory;
import org.geotools.fx.data.model.Parameter;
import org.geotools.fx.data.model.ParameterDescriptor;

@Accessors(fluent = true)
public class DataStoreEditor extends Control {

    private final @Getter ObjectProperty<DataStoreFactory> factoryProperty;
    private final @Getter ListProperty<Parameter> parametersProperty;
    private final @Getter BooleanProperty showAdvancedParametersProperty;
    private final @Getter BooleanProperty showProgramParametersProperty;

    protected @Override Skin<DataStoreEditor> createDefaultSkin() {
        return new DataStoreEditorSkin(this);
    }

    public DataStoreEditor() {
        super();

        factoryProperty = new SimpleObjectProperty<>(this, "factory");
        parametersProperty = new SimpleListProperty<>(this, "parameters");
        showAdvancedParametersProperty =
                new SimpleBooleanProperty(this, "showAdvancedParameters", true);
        showProgramParametersProperty =
                new SimpleBooleanProperty(this, "showProgramParameters", false);

        factoryProperty.addListener(
                (p, oldVal, factory) -> {
                    ObservableList<Parameter> params = FXCollections.observableArrayList();
                    if (factory != null) {
                        for (ParameterDescriptor pd : factory.getParameterDescriptors()) {
                            params.add(new Parameter(pd));
                        }
                    }
                    parametersProperty.set(params);
                });
    }

    public DataStoreFactory getFactory() {
        return factoryProperty.get();
    }

    public void setFactory(DataStoreFactory factory) {
        factoryProperty.set(factory);
    }

    public void setShowAdvancedParametersProperty(boolean show) {
        showAdvancedParametersProperty.set(show);
    }

    public boolean isShowAdvancedParametersProperty() {
        return showAdvancedParametersProperty.get();
    }

    public void setShowProgramParametersProperty(boolean show) {
        showProgramParametersProperty.set(show);
    }

    public boolean isShowProgramParametersProperty() {
        return showProgramParametersProperty.get();
    }
}
