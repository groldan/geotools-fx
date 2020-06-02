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

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;
import static org.awaitility.Durations.TWO_SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.geotools.fx.crs.model.CoordinateSystemRegistry;
import org.geotools.referencing.CRS;
import org.geotools.referencing.factory.epsg.FactoryUsingWKT;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class CoordinateSystemRegistryLoaderServiceTest {

    private CoordinateSystemRegistryLoaderService service;

    private ObjectProperty<CoordinateSystemRegistry> value = new SimpleObjectProperty<>();

    private ObjectProperty<Throwable> exception = new SimpleObjectProperty<>();

    private BooleanProperty running = new SimpleBooleanProperty();

    public static @BeforeClass void beforeClass() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException toolkitAlreadyInitialized) {

        }
    }

    public @Before void before() {
        service = new CoordinateSystemRegistryLoaderService();
        value.unbind();
        exception.unbind();
        running.unbind();
        Platform.runLater(
                () -> {
                    value.bind(service.valueProperty());
                    exception.bind(service.exceptionProperty());
                    running.bind(service.runningProperty());
                });
        await().atMost(ONE_SECOND).until(() -> running.isBound());
    }

    private CoordinateSystemRegistry runAndWait() throws Throwable {
        final AtomicBoolean finished = new AtomicBoolean(false);

        Platform.runLater(
                () -> {
                    service.setOnFailed(event -> finished.set(true));
                    service.setOnSucceeded(event -> finished.set(true));
                    service.start();
                });

        await().atMost(2, TimeUnit.MINUTES).untilTrue(finished);

        if (exception.get() != null) {
            throw exception.get();
        }
        return value.get();
    }

    public @Test final void testLoadProvided() throws Throwable {
        Properties props = new Properties();
        try (InputStream is = FactoryUsingWKT.class.getResourceAsStream("epsg.properties")) {
            props.load(is);
        }
        final List<String> codes =
                props.stringPropertyNames()
                        .stream()
                        .map(code -> String.format("EPSG:%s", code))
                        .collect(Collectors.toList());
        assertFalse(codes.isEmpty());
        service.setProvidedCodes(codes);

        AtomicInteger reportedProgress;
        reportedProgress = new AtomicInteger();
        Platform.runLater(
                () ->
                        service.workDoneProperty()
                                .addListener((p, o, n) -> reportedProgress.set(n.intValue())));

        CoordinateSystemRegistry registry = runAndWait();
        assertEquals(codes.size(), registry.size());
        await().atMost(TWO_SECONDS).until(() -> reportedProgress.get() > 1);
        assertEquals(codes.size(), reportedProgress.get());
    }

    @Ignore
    public @Test final void testLoadAll() throws Throwable {
        Set<String> crsCodes =
                CRS.getAuthorityFactory(false).getAuthorityCodes(CoordinateReferenceSystem.class);
        CoordinateSystemRegistry registry = runAndWait();
        assertEquals(crsCodes.size(), registry.size());
    }
}
