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
package org.geotools.fx.data.controls.internal;

import java.util.Optional;
import javafx.beans.value.ObservableValue;
import lombok.NonNull;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.PropertyEditor;
import org.geotools.fx.data.model.Parameter;
import org.geotools.fx.data.model.ParameterDescriptor.Level;

public class ParameterItem implements PropertySheet.Item {

    private Parameter param;

    private Class<? extends PropertyEditor<?>> propertyEditorClass;

    public ParameterItem(@NonNull Parameter param) {
        this.param = param;
        if (param.getDescriptor().isPassword()) {
            propertyEditorClass = PropertyEditors.PasswordEditor.class;
        }
    }

    public static Item of(Parameter p) {
        return new ParameterItem(p);
    }

    public @Override Class<?> getType() {
        return param.getDescriptor().getType();
    }

    public @Override String getCategory() {
        Level level = param.getDescriptor().getLevel();
        String displayName = level.displayName();
        if (level == Level.USER) {
            // HACK: PropertySheet sorts categories alphabetically
            final String ZERO_WIDTH_SPACE = " "; // "\u200A";
            displayName = ZERO_WIDTH_SPACE + displayName;
        }
        return displayName;
    }

    public @Override String getName() {
        return param.getDescriptor().getName();
    }

    public @Override String getDescription() {
        return param.getDescriptor().getDescription();
    }

    public @Override Object getValue() {
        return param.getValue();
    }

    public @Override void setValue(Object value) {
        param.setValue(value);
    }

    public @Override Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(param.valueProperty());
    }

    public @Override Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
        return Optional.ofNullable(propertyEditorClass);
    }
}
