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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/** TODO: avoid the same layer to be added twice? */
@Accessors(fluent = true)
public class MapModel {

    private static final Logger LOGGER = Logging.getLogger(MapModel.class);

    private final @Getter ObjectProperty<MapLayer> baseLayerProperty =
            new SimpleObjectProperty<>(this, "baseLayer");

    public Optional<MapLayer> baseLayer() {
        return Optional.ofNullable(baseLayerProperty.get());
    }

    public @Nullable MapLayer getBaseLayer() {
        return baseLayerProperty.get();
    }

    public void setBaseLayer(@Nullable MapLayer baseLayer) {
        baseLayerProperty.set(baseLayer);
    }

    private final @Getter ListProperty<MapLayer> layersProperty =
            new SimpleListProperty<>(this, "layers", FXCollections.observableArrayList());

    /** The layers in the map, indexed in the order they're overlaid/rendered */
    public ObservableList<MapLayer> getLayers() {
        return layersProperty.get();
    }

    public void setLayers(@NonNull ObservableList<MapLayer> layers) {
        layersProperty.set(layers);
    }

    public void addLayer(@NonNull MapLayer layer) {
        layersProperty.add(layer);
    }

    private final @Getter ReadOnlyObjectProperty<Viewport> viewportProperty =
            new SimpleObjectProperty<>(this, "viewport", new Viewport());

    public @NonNull Viewport getViewport() {
        return viewportProperty.get();
    }

    private ReferencedEnvelope forcedMaxBounds;

    public void setMaxBounds(@Nullable ReferencedEnvelope maxBounds) {
        this.forcedMaxBounds = maxBounds;
    }

    /**
     * Get the bounding box of all the layers in this Map. If all the layers cannot determine the
     * bounding box in the speed required for each layer, then null is returned. The bounds will be
     * expressed in the Map coordinate system.
     *
     * @return The bounding box of the features or null if unknown and too expensive for the method
     *     to calculate.
     * @throws IOException if an IOException occurs while accessing the FeatureSource bounds
     */
    public ReferencedEnvelope getMaxBounds() {
        if (forcedMaxBounds != null) {
            return forcedMaxBounds;
        }

        CoordinateReferenceSystem mapCrs = getViewport().getCoordinateReferenceSystem();
        ReferencedEnvelope maxBounds = null;

        for (MapLayer layer : layersProperty.get()) {
            if (layer == null) {
                continue;
            }
            try {
                ReferencedEnvelope layerBounds = layer.bounds().orElse(null);
                if (layerBounds == null || layerBounds.isEmpty() || layerBounds.isNull()) {
                    continue;
                }
                if (mapCrs == null || DefaultEngineeringCRS.CARTESIAN_2D.equals(mapCrs)) {
                    // crs for the map is not defined; let us start with the first CRS we see then!
                    maxBounds = new ReferencedEnvelope(layerBounds);
                    mapCrs = layerBounds.getCoordinateReferenceSystem();
                    continue;
                }
                ReferencedEnvelope normalized;
                if (CRS.equalsIgnoreMetadata(mapCrs, layerBounds.getCoordinateReferenceSystem())) {
                    normalized = layerBounds;
                } else {
                    try {
                        normalized = layerBounds.transform(mapCrs, true);
                    } catch (Exception e) {
                        LOGGER.log(Level.FINE, "Unable to transform: {0}", e);
                        continue;
                    }
                }
                if (maxBounds == null) {
                    maxBounds = normalized;
                } else {
                    maxBounds.expandToInclude(normalized);
                }
            } catch (Throwable eek) {
                LOGGER.log(Level.WARNING, "Unable to determine bounds of " + layer, eek);
            }
        }
        if (maxBounds == null) {
            maxBounds = new ReferencedEnvelope(mapCrs);
        }

        return maxBounds;
    }
}
