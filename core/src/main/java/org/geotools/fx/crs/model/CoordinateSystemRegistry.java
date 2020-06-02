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
package org.geotools.fx.crs.model;

import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.ENGINEERING;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.GEOCENTRIC;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.GEOGRAPHIC;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.IMAGE;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.PROJECTED;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.TEMPORAL;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.UNKNOWN;
import static org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType.VERTICAL;

import java.util.EnumSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.fx.crs.model.CRSInfo.CoordinateSystemType;

@Accessors(fluent = true)
public class CoordinateSystemRegistry {

    private static final EnumSet<CoordinateSystemType> SUPPORTED_TYPES =
            EnumSet.of(GEOGRAPHIC, PROJECTED);

    private static final EnumSet<CoordinateSystemType> UNSUPPORTED_TYPES =
            EnumSet.of(ENGINEERING, GEOCENTRIC, IMAGE, TEMPORAL, VERTICAL, UNKNOWN);

    private ReadOnlySetProperty<CRSInfo> supported;

    private ReadOnlySetProperty<CRSInfo> unsupported;

    private @Getter ReadOnlyListProperty<CRSInfo> all =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObservableMap<String, CRSInfo> map = FXCollections.observableMap(new TreeMap<>());

    public CoordinateSystemRegistry() {
        MapChangeListener<? super String, ? super CRSInfo> listener =
                new MapChangeListener<String, CRSInfo>() {
                    public @Override void onChanged(
                            Change<? extends String, ? extends CRSInfo> change) {
                        if (change.wasAdded()) {
                            addToProperty(change.getValueAdded());
                        } else if (change.wasRemoved()) {
                            removeFromProperty(change.getValueRemoved());
                        }
                    }
                };
        map.addListener(listener);
    }

    public boolean contains(@NonNull CRSInfo crs) {
        return containsAuthorityCode(crs.getAuthorityCode());
    }

    public boolean containsAuthorityCode(@NonNull String authorityCode) {
        return map.containsKey(authorityCode);
    }

    public static CoordinateSystemRegistry newInstance() {
        return new CoordinateSystemRegistry();
    }

    public void addAll(@NonNull Iterable<CRSInfo> crss) {
        for (CRSInfo crs : crss) {
            add(crs);
        }
    }

    public CRSInfo remove(@NonNull CRSInfo crs) {
        String authorityCode = crs.getAuthorityCode();
        return remove(authorityCode);
    }

    public CRSInfo remove(@NonNull String authorityCode) {
        CRSInfo removed = map.remove(authorityCode);
        if (removed != null) {
            all.remove(removed);
        }
        return removed;
    }

    public boolean add(@NonNull CRSInfo crs) {
        CRSInfo old = map.put(crs.getAuthorityCode(), crs);
        if (old != null) {
            all.remove(old);
        }
        all.add(crs);
        return old == null;
    }

    public int size() {
        return map.size();
    }

    public ReadOnlySetProperty<CRSInfo> supported() {
        if (supported == null) {
            supported = newSet("supported", SUPPORTED_TYPES);
        }
        return supported;
    }

    public ReadOnlySetProperty<CRSInfo> unsupported() {
        if (unsupported == null) {
            unsupported = newSet("unsupported", UNSUPPORTED_TYPES);
        }
        return unsupported;
    }

    private void removeFromProperty(@NonNull CRSInfo crs) {
        SetProperty<CRSInfo> prop = propertyFor(crs);
        if (prop != null) {
            prop.add(crs);
        }
    }

    private void addToProperty(@NonNull CRSInfo crs) {
        SetProperty<CRSInfo> prop = propertyFor(crs);
        if (prop != null) {
            prop.remove(crs);
        }
    }

    private ReadOnlySetProperty<CRSInfo> newSet(
            @NonNull String propertyName, @NonNull EnumSet<CoordinateSystemType> of) {

        ObservableSet<CRSInfo> observableSet =
                map.values()
                        .stream()
                        .filter(crs -> of.contains(crs.getCoordinateSystemType()))
                        .collect(Collectors.toCollection(FXCollections::observableSet));
        return new SimpleSetProperty<>(observableSet);
    }

    private SetProperty<CRSInfo> propertyFor(@NonNull CRSInfo crs) {
        final CoordinateSystemType projectionType = crs.getCoordinateSystemType();
        if (SUPPORTED_TYPES.contains(projectionType)) {
            return (SetProperty<CRSInfo>) supported;
        }
        return (SetProperty<CRSInfo>) unsupported;
    }
}
