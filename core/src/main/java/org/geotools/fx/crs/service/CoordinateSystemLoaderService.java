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
package org.geotools.fx.crs.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.geotools.fx.crs.model.CRSInfo;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

@Accessors(fluent = true)
public class CoordinateSystemLoaderService extends Service<ObservableList<CRSInfo>> {

    private @Getter SetProperty<String> providedCodesProperty =
            new SimpleSetProperty<>(this, "providedCodes", FXCollections.observableSet());

    private @Getter BooleanProperty forceLongitudeFirstProperty =
            new SimpleBooleanProperty(this, "forceLongitudeFirst", false);

    public ObservableSet<String> getProvidedCodes() {
        return providedCodesProperty;
    }

    public void setProvidedCodes(@NonNull Collection<String> authorityCodes) {
        providedCodesProperty.clear();
        providedCodesProperty.addAll(authorityCodes);
    }

    protected @Override Task<ObservableList<CRSInfo>> createTask() {
        return new LoadCrsListTask(
                new ArrayList<>(providedCodesProperty), forceLongitudeFirstProperty.get());
    }

    public boolean isForceLongitudeFirst() {
        return forceLongitudeFirstProperty().get();
    }

    public void setForceLongitudeFirst(boolean longFirst) {
        forceLongitudeFirstProperty().set(longFirst);
    }

    private static @RequiredArgsConstructor class LoadCrsListTask
            extends Task<ObservableList<CRSInfo>> {

        private final List<String> providedCodes;

        private final boolean forceLongitudFirst;

        protected @Override ObservableList<CRSInfo> call() throws Exception {
            final Set<String> crsCodes;
            final CRSAuthorityFactory authorityFactory;
            authorityFactory = CRS.getAuthorityFactory(forceLongitudFirst);

            super.updateTitle("Loading coordinate reference systems");

            if (this.providedCodes.isEmpty()) {
                crsCodes = authorityFactory.getAuthorityCodes(CoordinateReferenceSystem.class);
            } else {
                crsCodes = new HashSet<>(providedCodes);
            }
            if (isCancelled()) {
                return null;
            }
            super.updateMessage(
                    String.format(
                            "%,d %s available coordinate reference systems. %s",
                            crsCodes.size(),
                            (providedCodes.isEmpty() ? "" : " provided "),
                            (forceLongitudFirst ? "Forcing CRS longitude forst axis order." : "")));
            super.updateProgress(-1L, crsCodes.size());

            final long total = crsCodes.size();
            final AtomicLong workDone = new AtomicLong();

            // not doing Stream.parallel() as the amount of locking inside the GeoTools CRS
            // subsystem makes it slower
            ObservableList<CRSInfo> crslist =
                    crsCodes.stream()
                            .map(code -> load(code, authorityFactory, workDone, total))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));

            return isCancelled() ? null : crslist;
        }

        private CRSInfo load(
                @NonNull String crsCode,
                @NonNull CRSAuthorityFactory authorityFactory,
                @NonNull AtomicLong workDone,
                long total) {
            if (isCancelled()) {
                return null;
            }

            CRSInfo info;
            try {
                final CoordinateReferenceSystem crs;
                crs = authorityFactory.createCoordinateReferenceSystem(crsCode);
                info = new CRSInfo(crsCode, crs);
            } catch (Exception eaten) {
                String description = null;
                try {
                    description = authorityFactory.getDescriptionText(crsCode).toString();
                } catch (Exception e2) {

                }
                String reason = eaten.getMessage();
                info = CRSInfo.unsupported(crsCode, description, reason);
            }
            super.updateProgress(workDone.incrementAndGet(), total);
            return info;
        }
    }
}
