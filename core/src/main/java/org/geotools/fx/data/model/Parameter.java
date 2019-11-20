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

import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import org.geotools.util.Converters;

@Log
@Value
@ToString(onlyExplicitlyIncluded = true)
@Accessors(fluent = true)
public class Parameter {

    private final ReadOnlyObjectProperty<ParameterDescriptor> descriptorProperty =
            new SimpleObjectProperty<>(this, "descriptor");

    @ToString.Include(name = "descriptor")
    public ParameterDescriptor getDescriptor() {
        return descriptorProperty.get();
    }

    private final ObjectProperty<Object> valueProperty =
            new SimpleObjectProperty<>(this, "value") {
                public @Override void set(Object newValue) {
                    Object value = Converters.convert(newValue, getDescriptor().getType());
                    if (value != newValue && log.isLoggable(Level.FINE)) {
                        log.fine(
                                String.format(
                                        "Parameter %s converted from %s to %s",
                                        getDescriptor().getName(),
                                        (newValue == null ? null : newValue.getClass().getName()),
                                        (value == null ? null : value.getClass().getName())));
                    }
                    super.set(value);
                }
            };

    @ToString.Include(name = "value")
    public Object getValue() {
        return valueProperty.get();
    }

    public void setValue(Object value) {
        valueProperty.set(value);
    }

    public Parameter(ParameterDescriptor descriptor) {
        this(descriptor, descriptor.defaultValueProperty().get());
    }

    public Parameter(@NonNull ParameterDescriptor descriptor, Object value) {
        ((ObjectProperty<ParameterDescriptor>) descriptorProperty).set(descriptor);
        setValue(value);
    }
}
