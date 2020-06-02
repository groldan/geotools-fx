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
package org.geotools.fx.data.controls.internal;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class FileInputField extends Control {

    private final @Getter BooleanProperty selectDirectoryProperty =
            new SimpleBooleanProperty(this, "isDirectory", false);

    private final @Getter ObjectProperty<File> valueProperty =
            new SimpleObjectProperty<>(this, "value");

    private final @Getter StringProperty titleProperty =
            new SimpleStringProperty(this, "title", "Selecte file");

    public FileInputField() {
        super();
    }

    protected @Override Skin<FileInputField> createDefaultSkin() {
        return new FileInputFieldSkin(this);
    }
}
