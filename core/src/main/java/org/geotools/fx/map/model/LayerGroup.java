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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.geometry.jts.ReferencedEnvelope;

@Accessors(fluent = true)
public class LayerGroup extends MapLayer {

    private final @Getter ListProperty<MapLayer> layersProperty =
            new SimpleListProperty<>(this, "layers", FXCollections.observableArrayList());

    public ObservableList<MapLayer> getLayers() {
        return layersProperty.get();
    }

    public @Override String toString() {
        ObservableList<MapLayer> layers = getLayers();
        return String.format("%s %s", getTitle(), layers);
    }

    public void setLayers(@NonNull ObservableList<MapLayer> layers) {
        layersProperty.set(layers);
    }

    private final @Getter BooleanProperty mutuallyExclussiveVisibilityProperty =
            new SimpleBooleanProperty(this, "mutuallyExclussiveVisibility");

    public boolean isMutuallyExclussiveVisibility() {
        return mutuallyExclussiveVisibilityProperty.get();
    }

    public void setMutuallyExclussiveVisibility(boolean mutuallyExclusive) {
        mutuallyExclussiveVisibilityProperty.set(mutuallyExclusive);
    }

    private IdentityHashMap<MapLayer, VisibilityListener> visibilityListeners =
            new IdentityHashMap<>();

    public LayerGroup() {
        mutuallyExclussiveVisibilityProperty.addListener(this::mutuallyExclussiveVisibilityChanged);
        layersProperty.addListener(this::layersChanged);
    }

    public @Override @Nullable ReferencedEnvelope getBounds() {

        List<ReferencedEnvelope> allBounds =
                layersProperty.stream() //
                        .map((l) -> l.bounds().orElse(null)) //
                        .filter((b) -> b != null) //
                        .collect(Collectors.toList());
        // TODO: reproject
        if (allBounds.isEmpty()) {
            return null;
        }
        ReferencedEnvelope env = allBounds.get(0);
        for (int i = 1; i < allBounds.size(); i++) {
            ReferencedEnvelope b = allBounds.get(i);
            if (env.isEmpty() || env.isNull()) {
                env = b;
            } else {
                env.expandToInclude(b.getMinX(), b.getMinY());
                env.expandToInclude(b.getMaxX(), b.getMaxY());
            }
        }
        return env;
    }

    private void layersChanged(ListChangeListener.Change<? extends MapLayer> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList()
                        .forEach(
                                (l) -> {
                                    VisibilityListener vl = new VisibilityListener(this, l);
                                    l.visibleProperty().addListener(vl);
                                    this.visibilityListeners.put(l, vl);
                                });
            } else if (change.wasRemoved()) {
                change.getRemoved()
                        .forEach(
                                l -> {
                                    VisibilityListener vl = this.visibilityListeners.remove(l);
                                    Objects.requireNonNull(vl);
                                    l.visibleProperty().removeListener(vl);
                                });
            }
        }
        final boolean mutuallyExclusive = this.mutuallyExclussiveVisibilityProperty.get();
        if (mutuallyExclusive) {
            layersProperty.stream() //
                    .filter((l) -> l.visibleProperty().get()) //
                    .skip(1) //
                    .forEach((l) -> l.visibleProperty().set(false));
        }
    }

    /**
     * when mutually exclusive is set to false, does nothing, when set to true, leaves visible only
     * the first layer in the list that's already visible
     */
    private void mutuallyExclussiveVisibilityChanged(
            ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) {
        if (newValue.booleanValue()) {
            layersProperty.stream() //
                    .filter((l) -> l.visibleProperty().get()) //
                    .skip(1) //
                    .forEach((l) -> l.visibleProperty().set(false));
        }
    }

    private static class VisibilityListener implements ChangeListener<Boolean> {

        private LayerGroup group;

        private MapLayer layer;

        public VisibilityListener(LayerGroup group, MapLayer layer) {
            this.group = group;
            this.layer = layer;
        }

        @Override
        public void changed(
                ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue.booleanValue() && group.mutuallyExclussiveVisibilityProperty.get()) {
                group.layersProperty.forEach(
                        (l) -> {
                            if (l != this.layer) {
                                l.visibleProperty().set(false);
                            }
                        });
            } else if (!newValue.booleanValue()) {
                long visibleCount =
                        group.layersProperty.stream()
                                .filter((l) -> l.visibleProperty().get())
                                .count();
                if (0 == visibleCount) {
                    group.visibleProperty().set(false);
                }
            }
        }
    }
}
