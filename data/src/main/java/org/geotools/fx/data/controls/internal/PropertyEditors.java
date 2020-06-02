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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.util.StringConverter;
import lombok.NonNull;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

class PropertyEditors {

    public static PropertyEditor<?> createTimezoneEditor(@NonNull Item item) {
        List<String> ids = Arrays.asList(TimeZone.getAvailableIDs());
        ObservableList<TimeZone> timeZones = FXCollections.observableArrayList();
        for (String id : ids) {
            TimeZone tz = TimeZone.getTimeZone(id);
            timeZones.add(tz);
        }
        PropertyEditor<?> choiceEditor = Editors.createChoiceEditor(item, timeZones);
        @SuppressWarnings("unchecked")
        ComboBox<TimeZone> combo = (ComboBox<TimeZone>) choiceEditor.getEditor();
        combo.setConverter(
                new StringConverter<TimeZone>() {
                    public @Override String toString(TimeZone tz) {
                        return String.format(
                                "%s (%s)", tz.getID(), tz.getDisplayName(true, TimeZone.LONG));
                    }

                    public @Override TimeZone fromString(String string) {
                        throw new UnsupportedOperationException();
                    }
                });
        return choiceEditor;
    }

    public static PropertyEditor<?> createInputFileEditor(@NonNull Item item) {
        return new InputFileEditor(item, false);
    }

    public static PropertyEditor<?> createInputDirectoryEditor(@NonNull Item item) {
        return new InputFileEditor(item, true);
    }

    private static class InputFileEditor extends AbstractPropertyEditor<File, FileInputField> {

        public InputFileEditor(Item property, boolean directory) {
            super(property, new FileInputField());
            getEditor().titleProperty().set("Select " + property.getName());
            getEditor().selectDirectoryProperty().set(directory);
        }

        public @Override void setValue(File value) {
            getEditor().valueProperty().set(value);
        }

        protected @Override ObservableValue<File> getObservableValue() {
            return getEditor().valueProperty();
        }
    }

    public static PropertyEditor<?> createCharsetEditor(@NonNull Item item) {

        final Collection<Charset> availableCharsets = Charset.availableCharsets().values();
        PropertyEditor<?> choiceEditor = Editors.createChoiceEditor(item, availableCharsets);

        @SuppressWarnings("unchecked")
        ComboBox<Charset> combo = (ComboBox<Charset>) choiceEditor.getEditor();
        combo.setConverter(
                new StringConverter<Charset>() {
                    public @Override String toString(Charset object) {
                        return object.displayName();
                    }

                    public @Override Charset fromString(String name) {
                        return Charset.forName(name);
                    }
                });
        return choiceEditor;
    }

    public static class PasswordEditor extends AbstractPropertyEditor<String, PasswordField> {

        public PasswordEditor(Item property) {
            super(property, new PasswordField());
        }

        public @Override void setValue(String value) {
            getEditor().setText(value);
        }

        protected @Override ObservableValue<String> getObservableValue() {
            return getEditor().textProperty();
        }
    }
}
