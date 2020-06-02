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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapBoundsEvent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * Properties
 *
 * <ul>
 *   <li>{@link #fitBoundsProperty() FitBounds}: the bounds requested to
 *   <li>{@link #boundsProperty() Bounds}: read only, the current actual bounds of the map viewport,
 *       adjusted when any of the following properties change:
 * </ul>
 */
public class Viewport {

    private final ReadOnlyObjectProperty<Affine> worldToScreen =
            new SimpleObjectProperty<>(this, "World to scren transform");

    private final ReadOnlyObjectProperty<Affine> screenToWorld =
            new SimpleObjectProperty<>(this, "Screen to world transform");

    private final ReadOnlyObjectProperty<Display> display =
            new SimpleObjectProperty<>(this, "Display", new Display());

    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(this, "Scale");

    private final DoubleBinding boundScale;

    private final ObjectProperty<CoordinateReferenceSystem> coordinateReferenceSystem =
            new SimpleObjectProperty<>();

    private final SimpleObjectProperty<ReferencedEnvelope> bounds =
            new SimpleObjectProperty<>() {
                public @Override ReferencedEnvelope get() {
                    ReferencedEnvelope env = super.get();
                    return env == null ? new ReferencedEnvelope() : new ReferencedEnvelope(env);
                }
            };

    private final ObjectProperty<ReferencedEnvelope> fitBounds = new SimpleObjectProperty<>();

    private final BooleanExpression empty =
            new SimpleBooleanProperty(this, "Empty") {
                public @Override boolean get() {
                    ReferencedEnvelope env = bounds.get();
                    Bounds area = display.get().getArea();
                    return env == null
                            || env.isNull()
                            || env.isEmpty()
                            || area == null
                            || area.isEmpty();
                }
            };

    private final org.geotools.map.MapViewport delegate;

    public Viewport() {
        delegate = new org.geotools.map.MapViewport();
        delegate.setEditable(true);
        delegate.setMatchingAspectRatio(true);

        coordinateReferenceSystem.addListener(
                (p, o, n) -> delegate.setCoordinateReferenceSystem(n));

        final Display display = getDisplay();
        display.areaProperty()
                .addListener((p, o, n) -> delegate.setScreenArea(getDisplay().toAwtRectangle()));

        fitBounds.addListener((p, o, n) -> delegate.setBounds(n));

        delegate.addMapBoundsListener(this::mapBoundsUpdated);

        // scale depends on the bounds, screen size, dpi, and scaleX/scaleY properties
        boundScale =
                Bindings.createDoubleBinding(
                        this::calculateScale,
                        display.dpiProperty(),
                        bounds,
                        display.areaProperty(),
                        display.scaleXProperty(),
                        display.scaleYProperty());
        boundScale.addListener(
                (p, o, n) -> {
                    if (!scale.isBound()) {
                        scale.set(n.doubleValue());
                    }
                });
        scale.addListener((p, o, n) -> {});
    }

    private void mapBoundsUpdated(final MapBoundsEvent event) {
        Platform.runLater(
                () -> {
                    ReferencedEnvelope newBounds = event.getNewAreaOfInterest();
                    ((ObjectProperty<ReferencedEnvelope>) bounds).set(newBounds);
                    setCoordinateReferenceSystem(event.getNewCoordinateReferenceSystem());
                });
    }

    public void bindBidirectional(@NonNull Viewport vp2) {
        getDisplay().bindBidirectional(vp2.getDisplay());
        coordinateReferenceSystemProperty()
                .bindBidirectional(vp2.coordinateReferenceSystemProperty());
        fitBoundsProperty().bindBidirectional(vp2.fitBoundsProperty());
    }

    public void unbindBidirectional(@NonNull Viewport vp2) {
        getDisplay().unbindBidirectional(vp2.getDisplay());
        coordinateReferenceSystemProperty()
                .unbindBidirectional(vp2.coordinateReferenceSystemProperty());
        fitBoundsProperty().unbindBidirectional(vp2.fitBoundsProperty());
    }

    /**
     * Gets the current screen to world coordinate transform.
     *
     * @return a copy of the current screen to world transform or {@code null} if the transform is
     *     not set
     */
    public Affine getScreenToWorld() {
        return toFXAffine(delegate.getScreenToWorld());
    }

    /**
     * Gets the current world to screen coordinate transform.
     *
     * @return a copy of the current world to screen transform or {@code null} if the transform is
     *     not set
     */
    public Affine getWorldToScreen() {
        return toFXAffine(delegate.getWorldToScreen());
    }

    private Affine toFXAffine(@Nullable AffineTransform awttx) {
        if (awttx == null) {
            return new Affine();
        }
        double[] m = new double[6];
        awttx.getMatrix(m);
        // mxx = m00
        // mxy = m01
        // tx = m02
        // myx = m10
        // myy = m11
        // ty = m12
        return new Affine(m[0], m[2], m[4], m[1], m[3], m[5]);
    }

    public ReferencedEnvelope getBounds() {
        return boundsProperty().get();
    }

    public void setFitBounds(@Nullable ReferencedEnvelope env) {
        fitBoundsProperty().set(env);
    }

    public @Nullable ReferencedEnvelope getFitBounds() {
        return fitBoundsProperty().get();
    }

    public boolean isEmpty() {
        return emptyProperty().get();
    }

    public javafx.geometry.Point2D toDisplay(@NonNull javafx.geometry.Point2D worldCoord) {
        return getWorldToScreen().transform(worldCoord);
    }

    public javafx.geometry.Bounds toDisplay(@NonNull ReferencedEnvelope bounds) {
        CoordinateReferenceSystem crs = getCoordinateReferenceSystem();
        if (null != bounds.getCoordinateReferenceSystem()
                && !CRS.equalsIgnoreMetadata(crs, bounds.getCoordinateReferenceSystem())) {
            try {
                bounds = bounds.transform(crs, true);
            } catch (TransformException | FactoryException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return toDisplay(
                new BoundingBox(
                        bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
    }

    public javafx.geometry.Point2D toDisplay(double worldX, double worldY) {
        return toDisplay(new Point2D(worldX, worldY));
    }

    public javafx.geometry.Bounds toDisplay(@NonNull Bounds mapArea) {
        return getWorldToScreen().transform(mapArea);
    }

    public ReferencedEnvelope toWorld(@NonNull javafx.geometry.Bounds displayArea) {
        Bounds worldBounds = getScreenToWorld().transform(displayArea);
        ReferencedEnvelope bounds = new ReferencedEnvelope(getCoordinateReferenceSystem());
        bounds.init(
                worldBounds.getMinX(),
                worldBounds.getMaxX(),
                worldBounds.getMinY(),
                worldBounds.getMaxY());
        return bounds;
    }

    public javafx.geometry.Point2D toWorld(@NonNull javafx.geometry.Point2D displayCoord) {
        return getScreenToWorld().transform(displayCoord);
    }

    public javafx.geometry.Point2D toWorld(double displayX, double displaY) {
        return toWorld(new Point2D(displayX, displaY));
    }

    private @NonNull java.awt.Rectangle toAwtRectangle(@Nullable Bounds bounds) {
        if (bounds == null || bounds.isEmpty()) {
            return new java.awt.Rectangle();
        }
        return new java.awt.Rectangle(
                round(bounds.getMinX()),
                round(bounds.getMinY()),
                round(bounds.getWidth()),
                round(bounds.getHeight()));
    }

    private int round(double ordinate) {
        return (int) Math.round(ordinate);
    }

    private double calculateScale() {
        if (isEmpty()) {
            return -1;
        }
        final ReferencedEnvelope extent = getBounds();
        final Display display = getDisplay();
        final Bounds screenArea = display.getArea();
        final double dpi = display.getDpi();

        double scale;

        try {
            double displayWidth = screenArea.getWidth();
            double displayHeight = screenArea.getHeight();
            double displayScaleX = display.getScaleX();
            double displayScaleY = display.getScaleY();

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

    public ObjectExpression<Display> displayProperty() {
        return display;
    }

    public @NonNull Display getDisplay() {
        return displayProperty().get();
    }

    public DoubleExpression scaleProperty() {
        return scale;
    }

    public double getScale() {
        return scaleProperty().get();
    }

    public void setScale(double scale) {
        throw new UnsupportedOperationException("implement");
    }

    public ObjectProperty<CoordinateReferenceSystem> coordinateReferenceSystemProperty() {
        return coordinateReferenceSystem;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return coordinateReferenceSystemProperty().get();
    }

    public void setCoordinateReferenceSystem(CoordinateReferenceSystem crs) {
        coordinateReferenceSystemProperty().set(crs);
    }

    public ReadOnlyObjectProperty<ReferencedEnvelope> boundsProperty() {
        return bounds;
    }

    public ObjectProperty<ReferencedEnvelope> fitBoundsProperty() {
        return fitBounds;
    }

    public BooleanExpression emptyProperty() {
        return empty;
    }

    public ReadOnlyObjectProperty<Affine> worldToScreenTransformProperty() {
        return worldToScreen;
    }

    public ReadOnlyObjectProperty<Affine> screenToWorldTransformProperty() {
        return screenToWorld;
    }
}
