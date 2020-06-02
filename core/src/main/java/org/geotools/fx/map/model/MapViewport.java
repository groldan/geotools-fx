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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.transform.Transform;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

@Accessors(fluent = true)
public class MapViewport {

    private final DoubleProperty dpi = new SimpleDoubleProperty(90);

    private final DoubleExpression scale;

    private final @Getter DoubleProperty displayWidthProperty = new SimpleDoubleProperty();

    private final @Getter DoubleProperty displayHeightProperty = new SimpleDoubleProperty();

    private final @Getter DoubleProperty translateXProperty = new SimpleDoubleProperty(0.0);

    private final @Getter DoubleProperty translateYProperty = new SimpleDoubleProperty(0.0);

    private final @Getter DoubleProperty scaleXProperty = new SimpleDoubleProperty(1.0);

    private final @Getter DoubleProperty scaleYProperty = new SimpleDoubleProperty(1.0);

    private final @Getter ObjectExpression<Bounds> displayArea =
            Bindings.createObjectBinding(
                    () ->
                            new BoundingBox(
                                    0, 0, displayWidthProperty.get(), displayHeightProperty.get()),
                    displayWidthProperty,
                    displayHeightProperty);

    // separate projected bounds into crs and mapArea instead of a single
    // ReferencedEnvelope so that
    // it can't be changed oustide our control without firing notifications, since
    // ReferencedEnvelope is mutable
    private final ObjectProperty<Bounds> mapArea =
            new SimpleObjectProperty<>(new BoundingBox(0, 0, 0, 0));

    private final ObjectProperty<CoordinateReferenceSystem> crs =
            new SimpleObjectProperty<>(DefaultEngineeringCRS.CARTESIAN_2D);

    private final @Getter ObjectExpression<ReferencedEnvelope> mapBoundsProperty =
            new SimpleObjectProperty<>();

    private final BooleanProperty matchAspectRatio = new SimpleBooleanProperty(false);

    private @Getter ObjectProperty<Bounds> fitBoundsProperty = new SimpleObjectProperty<>();

    public MapViewport() {
        scale =
                Bindings.createDoubleBinding(
                        this::calculateScale, mapArea, dpi, crs, scaleXProperty, scaleYProperty);
        displayArea.addListener(
                (p, o, n) -> {
                    if (isNullOrEmpty(n)) {
                        return;
                    }
                    Bounds newBounds = calculateActualBounds(n, fitBoundsProperty.get());
                    mapArea.set(newBounds);
                });

        mapArea.addListener(
                (p, o, n) ->
                        ((ObjectProperty<ReferencedEnvelope>) mapBoundsProperty)
                                .set(getMapBounds()));

        fitBoundsProperty.addListener((p, o, n) -> setMapBounds(n));
    }

    public DoubleProperty dpi() {
        return dpi;
    }

    public DoubleExpression scale() {
        return scale;
    }

    private Double calculateScale() {
        ReferencedEnvelope extent = getMapBounds();
        Bounds screenArea = displayArea().get();
        double dpi = dpi().get();
        double scale;

        try {
            double displayWidth = screenArea.getWidth();
            double displayHeight = screenArea.getHeight();
            double displayScaleX = scaleXProperty().get();
            double displayScaleY = scaleYProperty().get();

            double scaledDisplayWidth = displayWidth * displayScaleX;
            double scaledDisplayHeight = displayHeight * displayScaleY;
            scale =
                    RendererUtilities.calculateScale(
                            extent,
                            (int) Math.round(scaledDisplayWidth),
                            (int) Math.round(scaledDisplayHeight),
                            dpi);
            if (scale > 1) {
                scale = Math.round(scale);
            }
        } catch (FactoryException | TransformException ex) {
            throw new RuntimeException("Failed to calculate scale", ex);
        }
        return scale;
    }

    public ReadOnlyObjectProperty<Bounds> mapArea() {
        return mapArea;
    }

    public ObjectProperty<CoordinateReferenceSystem> crs() {
        return crs;
    }

    public BooleanProperty matchingAspectRatio() {
        return matchAspectRatio;
    }

    public void setFitEnvelope(ReferencedEnvelope env) {
        if (null == env) {
            crs.set(DefaultEngineeringCRS.CARTESIAN_2D);
            mapArea.set(new BoundingBox(0, 0, 0, 0));
        } else {
            this.fitBoundsProperty.set(toRect(env));
            crs.set(env.getCoordinateReferenceSystem());
        }
    }

    public void setFitBounds(Bounds bounds) {
        fitBoundsProperty.set(bounds);
    }

    public Bounds getFitBounds() {
        return fitBoundsProperty.get();
    }

    private void setMapBounds(Bounds bounds) {
        if (isNullOrEmpty(bounds)) {
            return;
        }
        if (isNullOrEmpty(displayArea.get())) {
            mapArea.set(bounds);
            return;
        }
        if (null == bounds) {
            mapArea.set(new BoundingBox(0, 0, 0, 0));
        } else {
            // if (status == Status.READY && matchAspectRatio.get()) {
            bounds = calculateActualBounds(displayArea.get(), bounds);
            // }
        }
        if (Platform.isFxApplicationThread()) {
            mapArea.set(bounds);
        } else {
            final Bounds newMapArea = bounds;
            Platform.runLater(() -> mapArea.set(newMapArea));
        }
    }

    public void centerAt(javafx.geometry.Point2D displayAreaCenter) {
        javafx.geometry.Point2D newMapCenter =
                transform(displayAreaCenter.getX(), displayAreaCenter.getY(), getScreenToWorld());
        double centerX = newMapCenter.getX();
        double centerY = newMapCenter.getY();

        Bounds mapArea = fitBoundsProperty.get();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        BoundingBox newMapArea =
                new BoundingBox(centerX - width / 2, centerY - height / 2, width, height);
        this.fitBoundsProperty.set(newMapArea);
        mapArea = calculateActualBounds(displayArea.get(), newMapArea);
        this.mapArea.set(mapArea);
    }

    public ReferencedEnvelope getMapBounds() {
        Bounds ma = mapArea.get();
        return toReferencedEnvelope(ma);
    }

    public ReferencedEnvelope toReferencedEnvelope(Bounds ma) {
        return toReferencedEnvelope(ma, crs().get());
    }

    public ReferencedEnvelope toReferencedEnvelope(Bounds ma, CoordinateReferenceSystem crs) {
        double x1 = ma.getMinX();
        double x2 = ma.getMaxX();
        double y1 = ma.getMinY();
        double y2 = ma.getMaxY();
        return new ReferencedEnvelope(x1, x2, y1, y2, crs);
    }

    Bounds toRect(ReferencedEnvelope env) {
        return new BoundingBox(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }

    public javafx.geometry.Point2D toWorld(javafx.geometry.Point2D displayCoord) {
        return toWorld(displayCoord.getX(), displayCoord.getY());
    }

    public javafx.geometry.Point2D toWorld(double displayX, double displaY) {
        return transform(displayX, displaY, getScreenToWorld());
    }

    public javafx.geometry.Point2D toDisplay(javafx.geometry.Point2D worldCoord) {
        return toWorld(worldCoord.getX(), worldCoord.getY());
    }

    public javafx.geometry.Point2D toDisplay(double worldX, double worldY) {
        return transform(worldX, worldY, getWorldToScreen());
    }

    public javafx.geometry.Bounds toWorld(javafx.geometry.Bounds displayArea) {
        return transform(displayArea, getScreenToWorld());
    }

    public javafx.geometry.Bounds toDisplay(Bounds mapArea) {
        return transform(mapArea, getWorldToScreen());
    }

    private javafx.geometry.Bounds transform(Bounds rec, AffineTransform tx) {
        javafx.geometry.Point2D p1 = transform(rec.getMinX(), rec.getMinY(), tx);
        javafx.geometry.Point2D p2 = transform(rec.getMaxX(), rec.getMaxY(), tx);
        double minX = Math.min(p1.getX(), p2.getX());
        double minY = Math.min(p1.getY(), p2.getY());
        double maxX = Math.max(p1.getX(), p2.getX());
        double maxY = Math.max(p1.getY(), p2.getY());
        double width = maxX - minX;
        double height = maxY - minY;
        return new BoundingBox(minX, minY, width, height);
    }

    private javafx.geometry.Point2D transform(double x, double y, AffineTransform tx) {
        Point2D p0 = new Point2D.Double(x, y);
        tx.transform(p0, p0);
        return new javafx.geometry.Point2D(p0.getX(), p0.getY());
    }

    // private AffineTransform _wts = new AffineTransform(), _stw = new
    // AffineTransform();

    public AffineTransform getWorldToScreen() {
        return createAspectRatioMatchingWorldToScreen(displayArea.get(), mapArea.get());
    }

    public AffineTransform getScreenToWorld() {
        AffineTransform worldToScreen = getWorldToScreen();
        AffineTransform screenToWorld;
        try {
            screenToWorld = worldToScreen.createInverse();
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException("Unable to create coordinate transforms.", ex);
        }
        return screenToWorld;
    }

    private AffineTransform computeWorldToScreen() {
        return computeWorldToScreen(
                this.displayArea.get(), this.mapArea.get(), this.matchAspectRatio.get());
    }

    static AffineTransform computeWorldToScreen(
            Bounds displayArea, Bounds mapArea, boolean matchAspectRatio) {
        AffineTransform worldToScreen;
        if (isNullOrEmpty(displayArea) || isNullOrEmpty(mapArea)) {
            worldToScreen = new AffineTransform();
        } else if (matchAspectRatio) {
            worldToScreen = createAspectRatioMatchingWorldToScreen(displayArea, mapArea);
        } else {
            worldToScreen = calculateSimpleWorldToScreen(displayArea, mapArea);
        }
        Objects.requireNonNull(worldToScreen, "worldToScreen can be Identity but not null");
        return worldToScreen;
    }

    private AffineTransform computeScreenToWorld(AffineTransform worldToScreen) {
        Objects.requireNonNull(worldToScreen);
        AffineTransform screenToWorld;
        try {
            screenToWorld = worldToScreen.createInverse();
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException("Unable to create coordinate transforms.", ex);
        }
        return screenToWorld;
    }

    /**
     * Calculates transforms suitable for aspect ratio matching. The world bounds will be centered
     * in the screen area.
     */
    static AffineTransform createAspectRatioMatchingWorldToScreen(
            final Bounds displayArea, final Bounds mapArea) {
        if (displayArea.getWidth() == 0
                || displayArea.getHeight() == 0
                || mapArea.getWidth() == 0
                || mapArea.getHeight() == 0) {
            return new AffineTransform();
        }
        double displayWidth = displayArea.getWidth();
        double displayHeight = displayArea.getHeight();
        double xscale = displayWidth / mapArea.getWidth();
        double yscale = displayHeight / mapArea.getHeight();

        double scale = Math.min(xscale, yscale);

        double xoff = getCenterX(mapArea) * scale - getCenterX(displayArea);
        double yoff = getCenterY(mapArea) * scale + getCenterY(displayArea);

        AffineTransform worldToScreen = new AffineTransform(scale, 0, 0, -scale, -xoff, yoff);
        return worldToScreen;
    }

    /**
     * Calculates transforms suitable for no aspect ratio matching.
     *
     * @param requestedBounds requested display area in world coordinates
     * @return
     */
    static AffineTransform calculateSimpleWorldToScreen(Bounds displayArea, Bounds mapArea) {

        double xscale = displayArea.getWidth() / mapArea.getWidth();
        double yscale = displayArea.getHeight() / mapArea.getHeight();

        return new AffineTransform(
                xscale, 0, 0, -yscale, -xscale * mapArea.getMinX(), yscale * mapArea.getMaxY());
    }

    /** Calculates the world bounds of the current screen area. */
    private Bounds calculateActualBounds(final Bounds displayArea, final Bounds mapArea) {
        AffineTransform wts = computeWorldToScreen(displayArea, mapArea, true);
        final AffineTransform screenToWorld = computeScreenToWorld(wts);

        Point2D p0 = new Point2D.Double(displayArea.getMinX(), displayArea.getMinY());
        Point2D p1 = new Point2D.Double(displayArea.getMaxX(), displayArea.getMaxY());
        screenToWorld.transform(p0, p0);
        screenToWorld.transform(p1, p1);

        double minx = Math.min(p0.getX(), p1.getX());
        double maxx = Math.max(p0.getX(), p1.getX());
        double miny = Math.min(p0.getY(), p1.getY());
        double maxy = Math.max(p0.getY(), p1.getY());
        double width = maxx - minx;
        double height = maxy - miny;
        return new BoundingBox(minx, miny, width, height);
    }

    static double getCenterX(Bounds r) {
        return r.getMinX() + r.getWidth() / 2D;
    }

    static double getCenterY(Bounds r) {
        return r.getMinY() + r.getHeight() / 2D;
    }

    static boolean isNullOrEmpty(Bounds r) {
        return null == r || isEmpty(r);
    }

    static boolean isEmpty(Bounds r) {
        return r.getWidth() == 0.0 || r.getHeight() == 0.0;
    }

    public Bounds toDisplay(ReferencedEnvelope aoi) {
        return toDisplay(toRect(aoi));
    }

    /**
     * Applies the provided affine transform to the {@link #displayArea()}
     *
     * @param tx
     */
    public void screenTransform(Transform tx) {
        Bounds display = displayArea().get();
        Bounds transformed = tx.transform(display);
        Bounds mapBounts = toWorld(transformed);
        setFitBounds(mapBounts);
    }

    public double getDisplayWidth() {
        return displayWidthProperty.get();
    }

    public double getDisplayHeight() {
        return displayHeightProperty.get();
    }

    public double getDpi() {
        return dpi.get();
    }

    public void bindBidirectional(@NonNull MapViewport vp2) {
        translateXProperty().bindBidirectional(vp2.translateXProperty());
        translateYProperty().bindBidirectional(vp2.translateYProperty());
        scaleXProperty().bindBidirectional(vp2.scaleXProperty());
        scaleYProperty().bindBidirectional(vp2.scaleYProperty());
        crs().bindBidirectional(vp2.crs());
        fitBoundsProperty().bindBidirectional(vp2.fitBoundsProperty());
    }

    public void unbindBidirectional(@NonNull MapViewport vp2) {
        translateXProperty().unbindBidirectional(vp2.translateXProperty());
        translateYProperty().unbindBidirectional(vp2.translateYProperty());
        scaleXProperty().unbindBidirectional(vp2.scaleXProperty());
        scaleYProperty().unbindBidirectional(vp2.scaleYProperty());
        crs().unbindBidirectional(vp2.crs());
        fitBoundsProperty().unbindBidirectional(vp2.fitBoundsProperty());
    }
}
