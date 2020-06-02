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

import java.io.IOException;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.geotools.geometry.jts.ReferencedEnvelope;

@Accessors(fluent = true)
public abstract class MapLayer {

    private final @Getter StringProperty titleProperty;

    private final @Getter BooleanProperty visibleProperty;

    private final @Getter BooleanProperty queryableProperty;

    private final @Getter BooleanProperty selectedProperty;

    /**
     * The layer's opacity, as applied when stacking it's rendered surface. This is a value between
     * {@code 0.0} (fully transparent) and {@code 1.0} (fully opaque) and applies to the layer as a
     * whole, independently of any opacity applied to its styling rules.
     *
     * <p>TODO: add some sort of range validation?
     */
    private final @Getter DoubleProperty opacityProperty;

    public MapLayer() {
        titleProperty = new SimpleStringProperty(this, "title");
        visibleProperty = new SimpleBooleanProperty(this, "visible", true);
        queryableProperty = new SimpleBooleanProperty(this, "queryable", true);
        selectedProperty = new SimpleBooleanProperty(this, "selected", false);
        opacityProperty = new SimpleDoubleProperty(this, "opacity", 1.0);
    }

    public void setTitle(String title) {
        titleProperty.set(title);
    }

    public @Nullable String getTitle() {
        return titleProperty.get();
    }

    public Optional<String> title() {
        return Optional.ofNullable(getTitle());
    }

    public boolean isQueryable() {
        return queryableProperty.get();
    }

    public void setQueryable(boolean queryable) {
        queryableProperty.set(queryable);
    }

    public boolean isVisisble() {
        return visibleProperty.get();
    }

    public void setVisible(boolean visible) {
        visibleProperty.set(visible);
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public void setSelected(boolean selected) {
        selectedProperty.set(selected);
    }

    public double getOpacity() {
        return opacityProperty.get();
    }

    public void setOpacity(double opacity) {
        opacityProperty.set(opacity);
    }

    /**
     * Override of {@link Object#equals(Object)} to explicitly indicate equality check is reference
     * equality.
     */
    public @Override boolean equals(Object o) {
        return this == o;
    }

    /**
     * The bounds of the Layer content (if known). The bounds can be used to determine if any of the
     * layers content is "on screen" when rendering the map; however often it is expensive to
     * calculate a layers bounds up front so we are allowing this value to be optional.
     *
     * <p>The returned bounds are a ReferencedEnvelope using the same CoordinateReferenceSystem as
     * the layers contents.
     *
     * @return layer bounds, {@link Optional#empty()} if unknown or too expensive to calculate.
     */
    public Optional<ReferencedEnvelope> bounds() {
        try {
            return Optional.ofNullable(getBounds());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract ReferencedEnvelope getBounds() throws IOException;

    public @Override String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), getTitle());
    }
}
