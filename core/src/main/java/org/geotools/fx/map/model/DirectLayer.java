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

import java.util.function.BooleanSupplier;
import javafx.scene.canvas.GraphicsContext;

public abstract class DirectLayer extends MapLayer {

    public abstract void draw(GraphicsContext graphics, Viewport viewport, BooleanSupplier aborted);

    //    public abstract void draw(
    //            Graphics2D graphics, org.geotools.map.MapViewport viewport, BooleanSupplier
    // aborted);
}
