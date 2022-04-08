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

import static org.geotools.fx.data.model.ParameterDescriptor.Level.ADVANCED;
import static org.geotools.fx.data.model.ParameterDescriptor.Level.PROGRAM;
import static org.geotools.fx.data.model.ParameterDescriptor.Level.USER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.scene.control.SkinBase;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.geotools.fx.data.controls.internal.ParameterItem;
import org.geotools.fx.data.controls.internal.ParameterPropertyEditorFactory;
import org.geotools.fx.data.model.DataStoreFactory;
import org.geotools.fx.data.model.Parameter;
import org.geotools.fx.data.model.ParameterDescriptor.Level;

@Log
class DataStoreEditorSkin extends SkinBase<DataStoreEditor> {

    private PropertySheet propertySheet;

    private List<Item> userItems;

    private List<Item> advancedItems;

    private List<Item> programItems;

    protected DataStoreEditorSkin(@NonNull DataStoreEditor control) {
        super(control);
        propertySheet = new PropertySheet();
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setPropertyEditorFactory(new ParameterPropertyEditorFactory());
        getChildren().add(propertySheet);

        getSkinnable()
                .factoryProperty()
                .addListener((p, o, n) -> Platform.runLater(() -> updateFactory(n)));

        getSkinnable()
                .showAdvancedParametersProperty()
                .addListener((p, o, n) -> Platform.runLater(() -> updateParams(n, advancedItems)));

        getSkinnable()
                .showProgramParametersProperty()
                .addListener((p, o, n) -> Platform.runLater(() -> updateParams(n, programItems)));
    }

    private void updateFactory(DataStoreFactory factory) {
        propertySheet.getItems().clear();
        if (factory == null) {
            return;
        }
        ListProperty<Parameter> parameters = getSkinnable().parametersProperty();

        userItems = createCategoryItems(filterParams(parameters, USER));
        advancedItems = createCategoryItems(filterParams(parameters, ADVANCED));
        programItems = createCategoryItems(filterParams(parameters, PROGRAM));

        addItems(userItems);
        if (getSkinnable().showAdvancedParametersProperty().get()) {
            propertySheet.getItems().addAll(advancedItems);
        }
        if (getSkinnable().showProgramParametersProperty().get()) {
            propertySheet.getItems().addAll(programItems);
        }
    }

    private void addItems(List<Item> items) {
        for (Item item : items) {
            log.fine(
                    () ->
                            String.format(
                                    "Adding item %s:%s = %s",
                                    item.getName(), item.getType(), item.getValue()));
            propertySheet.getItems().add(item);
        }
    }

    private void updateParams(boolean show, List<Item> items) {
        if (null == items) {
            return;
        }
        if (show) {
            propertySheet.getItems().addAll(items);
        } else {
            propertySheet.getItems().removeAll(items);
        }
    }

    private List<Parameter> filterParams(ListProperty<Parameter> parameters, Level level) {
        return parameters.stream()
                .filter(p -> p.getDescriptor().getLevel() == level)
                .collect(Collectors.toList());
    }

    private List<Item> createCategoryItems(List<Parameter> params) {
        List<Item> items = new ArrayList<>();
        for (Parameter p : params) {
            items.add(ParameterItem.of(p));
        }
        return items;
    }
}
