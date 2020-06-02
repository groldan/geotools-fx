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

import static javafx.beans.binding.Bindings.createBooleanBinding;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javax.annotation.Nonnegative;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class Display {

    public void bindBidirectional(@NonNull Display vp2) {
        translateXProperty().bindBidirectional(vp2.translateXProperty());
        translateYProperty().bindBidirectional(vp2.translateYProperty());
        scaleXProperty().bindBidirectional(vp2.scaleXProperty());
        scaleYProperty().bindBidirectional(vp2.scaleYProperty());
    }

    public void unbindBidirectional(@NonNull Display vp2) {
        translateXProperty().unbindBidirectional(vp2.translateXProperty());
        translateYProperty().unbindBidirectional(vp2.translateYProperty());
        scaleXProperty().unbindBidirectional(vp2.scaleXProperty());
        scaleYProperty().unbindBidirectional(vp2.scaleYProperty());
    }

    public boolean isEmpty() {
        return emptyProperty.get();
    }

    public @NonNull Bounds getArea() {
        return areaProperty().get();
    }

    public void setArea(Bounds screenArea) {
        setWidth(screenArea == null ? 0d : screenArea.getWidth());
        setHeight(screenArea == null ? 0d : screenArea.getHeight());
    }

    public double getWidth() {
        return widthProperty().get();
    }

    public void setWidth(@Nonnegative double width) {
        widthProperty().set(width);
    }

    public double getHeight() {
        return heightProperty().get();
    }

    public void setHeight(@Nonnegative double height) {
        heightProperty().set(height);
    }

    public double getDpi() {
        return dpiProperty.get();
    }

    public void setDpi(double dotsPerInch) {
        dpiProperty.set(dotsPerInch);
    }

    public @NonNull java.awt.Rectangle toAwtRectangle() {
        Bounds bounds = getArea();
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

    private final @Getter DoubleProperty dpiProperty = new SimpleDoubleProperty(90);

    private final @Getter DoubleProperty widthProperty = new SimpleDoubleProperty(0.0);

    private final @Getter DoubleProperty heightProperty = new SimpleDoubleProperty(0.0);

    private final @Getter ObjectExpression<BoundingBox> areaProperty =
            Bindings.createObjectBinding(
                    () -> new BoundingBox(0d, 0d, widthProperty.get(), heightProperty.get()),
                    widthProperty,
                    heightProperty);

    private final @Getter BooleanExpression emptyProperty =
            createBooleanBinding(
                    () -> {
                        Bounds area = areaProperty.get();
                        return area == null || area.isEmpty();
                    },
                    areaProperty);

    public double getScaleX() {
        return scaleXProperty().get();
    }

    public double getScaleY() {
        return scaleYProperty().get();
    }

    public double getTranslateX() {
        return translateXProperty().get();
    }

    public double getTranslateY() {
        return translateYProperty().get();
    }

    public void setScaleX(double scaleX) {
        scaleXProperty().set(scaleX);
    }

    public void setScaleY(double scaleY) {
        scaleYProperty().set(scaleY);
    }

    public void setTranslateX(double translateX) {
        translateXProperty().set(translateX);
    }

    public void setTranslateY(double translateY) {
        translateYProperty().set(translateY);
    }

    private final @Getter DoubleProperty translateXProperty = new SimpleDoubleProperty(0.0);

    private final @Getter DoubleProperty translateYProperty = new SimpleDoubleProperty(0.0);

    private final @Getter DoubleProperty scaleXProperty = new SimpleDoubleProperty(1.0);

    private final @Getter DoubleProperty scaleYProperty = new SimpleDoubleProperty(1.0);
}
