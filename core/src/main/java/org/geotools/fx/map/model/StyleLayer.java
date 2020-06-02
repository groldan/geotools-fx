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
package org.geotools.fx.map.model;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.styling.Style;

@Accessors(fluent = true)
public abstract class StyleLayer extends MapLayer {

    private final @Getter ObjectProperty<Style> styleProperty = new SimpleObjectProperty<>();

    public void setStyle(@NonNull Style style) {
        this.styleProperty.set(style);
    }

    public @Nullable Style getStyle() {
        return this.styleProperty.get();
    }

    public Optional<Style> style() {
        return Optional.ofNullable(getStyle());
    }

    protected StyleLayer() {
        //
    }

    protected StyleLayer(@NonNull Style style) {
        setStyle(style);
    }
}
