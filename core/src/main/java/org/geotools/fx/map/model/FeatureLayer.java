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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;

@Accessors(fluent = true)
public class FeatureLayer extends StyleLayer {

    private final @Getter Property<FeatureSource<?, ?>> featureSourceProperty =
            new SimpleObjectProperty<>("this", "featureSource");

    public @Nullable FeatureSource<?, ?> getFeatureSource() {
        return featureSourceProperty.getValue();
    }

    public void setFeatureSource(@Nullable FeatureSource<?, ?> fs) {
        featureSourceProperty.setValue(fs);
    }

    public Optional<FeatureSource<?, ?>> featureSource() {
        return Optional.ofNullable(getFeatureSource());
    };

    public FeatureLayer() {
        super();
    }

    public FeatureLayer(@NonNull FeatureSource<?, ?> featureSource) {
        super();
        this.featureSourceProperty.setValue(featureSource);
    }

    public FeatureLayer(@NonNull FeatureSource<?, ?> featureSource, @NonNull Style style) {
        super(style);
        this.featureSourceProperty.setValue(featureSource);
    }

    public @Override ReferencedEnvelope getBounds() throws IOException {
        FeatureSource<?, ?> source = featureSourceProperty.getValue();
        return source == null ? null : source.getBounds();
    }
}
