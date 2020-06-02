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

import java.util.Collection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.geotools.fx.crs.model.CRSInfo;
import org.geotools.fx.crs.model.CoordinateSystemRegistry;

@Accessors(fluent = true)
public class CoordinateSystemRegistryLoaderService extends Service<CoordinateSystemRegistry> {

    private final @Getter SetProperty<String> providedCodesProperty =
            new SimpleSetProperty<>(this, "providedCodes", FXCollections.observableSet());

    private final @Getter BooleanProperty forceLongitudeFirstProperty =
            new SimpleBooleanProperty(this, "forceLongitudeFirst", false);

    private final @Getter ObjectProperty<CoordinateSystemLoaderService> loaderProperty =
            new SimpleObjectProperty<>();

    public CoordinateSystemRegistryLoaderService() {
        loaderProperty.addListener(
                (p, o, n) -> {
                    if (o != null) {
                        o.providedCodesProperty().unbind();
                        o.forceLongitudeFirstProperty().unbind();
                    }
                    if (n != null) {
                        n.providedCodesProperty().bind(providedCodesProperty);
                        n.forceLongitudeFirstProperty().bind(forceLongitudeFirstProperty);
                    }
                });
        loaderProperty.set(new CoordinateSystemLoaderService());
    }

    public CoordinateSystemLoaderService getLoader() {
        return loaderProperty().get();
    }

    public ObservableSet<String> getProvidedCodes() {
        return providedCodesProperty;
    }

    public void setProvidedCodes(Collection<String> authorityCodes) {
        providedCodesProperty.clear();
        if (authorityCodes != null) {
            providedCodesProperty.addAll(authorityCodes);
        }
    }

    protected @Override Task<CoordinateSystemRegistry> createTask() {
        CoordinateSystemLoaderService loader = getLoader();
        Task<ObservableList<CRSInfo>> listTask = loader.createTask();
        return new LoadCrsRegistryTask(listTask);
    }

    private static @RequiredArgsConstructor class LoadCrsRegistryTask
            extends Task<CoordinateSystemRegistry> {

        private final Task<ObservableList<CRSInfo>> listTask;

        protected @Override CoordinateSystemRegistry call() throws Exception {
            super.updateTitle("Loading coordinate reference systems");
            listTask.workDoneProperty().addListener(e -> updateProgress());
            listTask.totalWorkProperty().addListener(e -> updateProgress());
            listTask.run();
            ObservableList<CRSInfo> list = listTask.get();
            if (isCancelled()) {
                return null;
            }
            final CoordinateSystemRegistry registry = CoordinateSystemRegistry.newInstance();
            registry.addAll(list);
            return isCancelled() ? null : registry;
        }

        private void updateProgress() {
            super.updateProgress(listTask.getWorkDone(), listTask.getTotalWork());
        }
    }
}
