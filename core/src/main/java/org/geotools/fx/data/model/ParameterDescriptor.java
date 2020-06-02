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
package org.geotools.fx.data.model;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.geotools.data.DataAccessFactory.Param;
import org.opengis.util.InternationalString;

/** JavaFX bean adapter for {@link Param DataAccessFactory#Param} */
@ToString(onlyExplicitlyIncluded = true)
@Accessors(fluent = true)
public class ParameterDescriptor {

    public enum Level {
        USER("Parameters"),
        ADVANCED("Advanced settings"),
        PROGRAM("Program level settings");

        Level(String displayName) {
            this.displayName = displayName;
        }

        private final @Getter String displayName;

        private static Level valueOf(Param parameter) {
            Level l;
            final String level = parameter.getLevel();
            // HACK: some datastore factories don't advertise the (geoserver specific) "namespace"
            // parameter as a program level parameter
            final String name = parameter.getName();
            if ("namespace".equals(name)) {
                l = Level.PROGRAM;
            } else {
                switch (level) {
                    case "advanced":
                        l = Level.ADVANCED;
                        break;
                    case "program":
                        l = Level.PROGRAM;
                        break;
                    default:
                        l = Level.USER;
                }
            }
            return l;
        }
    }

    private final @Getter ReadOnlyObjectProperty<Param> paramProperty;
    private final @Getter ReadOnlyStringProperty nameProperty;
    private final @Getter ReadOnlyStringProperty titleProperty;
    private final @Getter ReadOnlyStringProperty descriptionProperty;
    private final @Getter ReadOnlyObjectProperty<Class<?>> typeProperty;
    private final @Getter ReadOnlyObjectProperty<Level> levelProperty;
    private final @Getter ReadOnlyBooleanProperty passwordProperty;
    private final @Getter ReadOnlyBooleanProperty requiredProperty;
    private final @Getter ReadOnlyBooleanProperty deprecatedProperty;
    private final @Getter ReadOnlyObjectProperty<Object> defaultValueProperty;

    public ParameterDescriptor(@NonNull Param parameter) {
        this.paramProperty = new SimpleObjectProperty<>(this, "param", parameter);
        InternationalString title = parameter.getTitle();
        InternationalString description = parameter.getDescription();

        this.nameProperty = new SimpleStringProperty(this, "name", parameter.getName());
        this.titleProperty = new SimpleStringProperty(this, "title", title.toString());
        this.descriptionProperty =
                new SimpleStringProperty(
                        this,
                        "description",
                        description == null ? null : parameter.getDescription().toString());
        this.levelProperty = new SimpleObjectProperty<>(this, "level", Level.valueOf(parameter));
        this.defaultValueProperty =
                new SimpleObjectProperty<>(this, "defaultValue", parameter.getDefaultValue());
        this.passwordProperty = new SimpleBooleanProperty(this, "password", parameter.isPassword());
        this.requiredProperty = new SimpleBooleanProperty(this, "required", parameter.isRequired());
        this.deprecatedProperty =
                new SimpleBooleanProperty(this, "deprecated", parameter.isDeprecated());
        this.typeProperty = new SimpleObjectProperty<Class<?>>(this, "type", parameter.getType());
    }

    public Param getParam() {
        return paramProperty.get();
    }

    @ToString.Include(name = "name")
    public String getName() {
        return nameProperty.get();
    }

    @ToString.Include(name = "title")
    public String getTitle() {
        return titleProperty.get();
    }

    @ToString.Include(name = "description")
    public String getDescription() {
        return descriptionProperty.get();
    }

    @ToString.Include(name = "type")
    public Class<?> getType() {
        return typeProperty.get();
    }

    @ToString.Include(name = "level")
    public Level getLevel() {
        return levelProperty.get();
    }

    @ToString.Include(name = "password")
    public boolean isPassword() {
        return passwordProperty.get();
    }

    @ToString.Include(name = "required")
    public boolean isRequired() {
        return requiredProperty.get();
    }

    @ToString.Include(name = "deprecated")
    public boolean isDeprecated() {
        return deprecatedProperty.get();
    }

    @ToString.Include(name = "defaultValue")
    public Object getDefaultValue() {
        return defaultValueProperty.get();
    }
}
