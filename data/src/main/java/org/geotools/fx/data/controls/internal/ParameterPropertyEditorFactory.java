/*
 * GeoTools - The Open Source Java GIS Toolkit http://geotools.org
 *
 * (C) 2019, Open Source Geospatial Foundation (OSGeo) (C)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.geotools.fx.data.controls.internal;

import java.io.File;
import java.nio.charset.Charset;
import java.util.TimeZone;
import lombok.NonNull;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

public class ParameterPropertyEditorFactory extends DefaultPropertyEditorFactory {

    public @Override PropertyEditor<?> call(@NonNull Item item) {
        final Class<?> type = item.getType();

        if (TimeZone.class.equals(type)) {
            return PropertyEditors.createTimezoneEditor(item);
        }

        if (File.class.equals(type)) {
            // HACK
            if ("directory".equalsIgnoreCase(item.getName())) {
                return PropertyEditors.createInputDirectoryEditor(item);
            }
            return PropertyEditors.createInputFileEditor(item);
        }
        if (Charset.class.equals(type)) {
            return PropertyEditors.createCharsetEditor(item);
        }
        return super.call(item);
    }
}
