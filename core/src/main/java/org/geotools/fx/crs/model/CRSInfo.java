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
package org.geotools.fx.crs.model;

import java.util.NoSuchElementException;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.measure.Unit;
import lombok.NonNull;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.FactoryRegistryException;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.Projection;

public class CRSInfo {

    private static final ReadOnlyObjectProperty<?> VOID = new ReadOnlyObjectWrapper<>();

    private static final ReadOnlyStringProperty EMPTYSTRING = new SimpleStringProperty();

    @SuppressWarnings("unchecked")
    private static <T> ReadOnlyProperty<T> emptyReadOnly() {
        return (ReadOnlyObjectProperty<T>) VOID;
    }

    private ReadOnlyStringProperty authorityCode = EMPTYSTRING;

    private ReadOnlyProperty<CoordinateReferenceSystem> crs = emptyReadOnly();

    private StringExpression description = EMPTYSTRING;

    private StringExpression scope;

    private StringExpression domainOfValidity;

    private ReadOnlyProperty<ReferencedEnvelope> areaOfValidity;

    private ReadOnlyIntegerProperty dimension;

    private StringExpression wkt;

    // CoordinateSystem properties
    private ObjectExpression<CoordinateSystem> coordinateSystem;

    private StringExpression axesDescription;

    private StringExpression unsupportedReason;

    /** Constructor for groups of coordinate reference systems */
    public CRSInfo(@NonNull String groupName) {
        this.description = new SimpleStringProperty(groupName);
        this.crs = new SimpleObjectProperty<>();
        this.authorityCode = new SimpleStringProperty();
        this.wkt = new SimpleStringProperty();
        this.coordinateSystem = new SimpleObjectProperty<>();
    }

    /** Constructor for supported coordinate reference systems */
    public CRSInfo(@NonNull String authorityCode, @NonNull CoordinateReferenceSystem coordRefSys) {
        this.authorityCode = new SimpleStringProperty(authorityCode);
        this.crs = new SimpleObjectProperty<>(coordRefSys);
        this.description = stringProp(crs.getValue().getName().getCode());
        this.wkt = Bindings.createStringBinding(() -> crs.getValue().toString(), crs);

        this.coordinateSystem =
                Bindings.createObjectBinding(() -> crs.getValue().getCoordinateSystem(), crs);

        this.axesDescription = Bindings.createStringBinding(() -> getUnitsStr(), coordinateSystem);
    }

    private StringExpression stringProp(Object o) {
        return o == null ? EMPTYSTRING : new SimpleStringProperty(string(o));
    }

    private String string(Object val) {
        return val == null ? null : val.toString();
    }

    /** constructor for unsupported coordinate reference systems */
    CRSInfo(String crsCode, String description, String reason) {
        this.authorityCode = new SimpleStringProperty(crsCode);
        this.description = new SimpleStringProperty(description);
        this.unsupportedReason = new SimpleStringProperty(reason);
    }

    public StringExpression unsupportedReasonProperty() {
        return unsupportedReason == null ? new SimpleStringProperty() : unsupportedReason;
    }

    public String getUnsupportedReason() {
        return unsupportedReasonProperty().get();
    }

    private String getUnitsStr() {
        CoordinateSystem cs = coordinateSystem.get();
        int dimension = cs.getDimension();
        String d = axisDesc(cs.getAxis(0));
        for (int i = 1; i < dimension; i++) {
            d += ", " + axisDesc(cs.getAxis(i));
        }
        return d;
    }

    private String axisDesc(CoordinateSystemAxis axis) {
        AxisDirection direction = axis.getDirection();
        String abbreviation = axis.getAbbreviation();
        Unit<?> unit = axis.getUnit();
        String directionArrow = axisDirectionIndicator(direction);
        return String.format("%s %s(%s)", directionArrow, abbreviation, unit);
    }

    private String axisDirectionIndicator(final AxisDirection direction) {
        switch (direction.name()) {
            case "OTHER":
                return "";
            case "NORTH":
            case "UP":
            case "DISPLAY_UP":
                return "\u2191";
            case "NORTH_NORTH_EAST":
            case "NORTH_EAST":
            case "EAST_NORTH_EAST":
                return "\u2197";
            case "EAST":
            case "DISPLAY_RIGHT":
                return "\u2192";
            case "EAST_SOUTH_EAST":
            case "SOUTH_EAST":
            case "SOUTH_SOUTH_EAST":
                return "\u2198";
            case "SOUTH":
            case "DOWN":
            case "DISPLAY_DOWN":
                return "\u2193";
            case "SOUTH_SOUTH_WEST":
            case "SOUTH_WEST":
            case "WEST_SOUTH_WEST":
                return "\u2199";
            case "WEST":
            case "DISPLAY_LEFT":
                return "\u2190";
            case "WEST_NORTH_WEST":
            case "NORTH_WEST":
            case "NORTH_NORTH_WEST":
                return "\u2196";
            case "GEOCENTRIC_Y":
            case "GEOCENTRIC_X":
            case "GEOCENTRIC_Z":
                return "";
            case "FUTURE":
                return "\u23F0\u2192";
            case "PAST":
                return "\u2190\u23F0";
            case "COLUMN_POSITIVE":
                return "\u229E\u2192";
            case "COLUMN_NEGATIVE":
                return "\u2190\u229E";
            case "ROW_POSITIVE":
                return "\u229E\u2191";
            case "ROW_NEGATIVE":
                return "\u229E\u2193";
        }
        return "";
    }

    public ReadOnlyProperty<CoordinateReferenceSystem> crsProperty() {
        return crs;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs.getValue();
    }

    public ReadOnlyStringProperty authorityCodeProperty() {
        return authorityCode;
    }

    public String getAuthorityCode() {
        return authorityCode.getValue();
    }

    public StringExpression descriptionProperty() {
        return description;
    }

    public String getDescription() {
        return description.get();
    }

    public StringExpression scopeProperty() {
        if (scope == null) {
            scope = stringProp(getScope());
        }
        return scope;
    }

    public String getScope() {
        CoordinateReferenceSystem cs = crs.getValue();
        return cs == null ? null : string(cs.getScope());
    }

    public StringExpression domainOfValidityProperty() {
        if (domainOfValidity == null) {
            domainOfValidity = stringProp(getDomainOfValidity());
        }
        return domainOfValidity;
    }

    public String getDomainOfValidity() {
        CoordinateReferenceSystem cs = crs.getValue();
        Extent extent = cs == null ? null : cs.getDomainOfValidity();
        return extent == null ? null : string(extent.getDescription());
    }

    public ReadOnlyProperty<ReferencedEnvelope> areaOfValidityProperty() {
        if (areaOfValidity == null) {
            final ReferencedEnvelope aov = getAreaOfValidityInternal();
            areaOfValidity = new SimpleObjectProperty<>(this, "areaOfValidity", aov);
        }
        return areaOfValidity;
    }

    public ReferencedEnvelope getAreaOfValidity() {
        return areaOfValidityProperty().getValue();
    }

    private ReferencedEnvelope getAreaOfValidityInternal() {
        org.opengis.geometry.Envelope envelope;
        try {
            final CoordinateReferenceSystem crs = getCrs();
            envelope = CRS.getEnvelope(crs);

            if (envelope == null) {
                if ("World_Mollweide".equals(crs.getName().toString())) {
                    envelope = new ReferencedEnvelope(crs);
                    ((ReferencedEnvelope) envelope).expandToInclude(-17857080, -9109896);
                    ((ReferencedEnvelope) envelope).expandToInclude(17894602, 9030773);
                } else if ("World_Eckert_IV".equals(crs.getName().toString())) {
                    envelope = new ReferencedEnvelope(crs);
                    ((ReferencedEnvelope) envelope).expandToInclude(-16795818, -8557608);
                    ((ReferencedEnvelope) envelope).expandToInclude(16831110, 8504944);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }

        return new ReferencedEnvelope(envelope);
    }

    public ReadOnlyIntegerProperty dimensionProperty() {
        if (dimension == null) {
            CoordinateReferenceSystem cs = crs.getValue();
            int dim = cs == null ? 0 : cs.getCoordinateSystem().getDimension();
            dimension = new SimpleIntegerProperty(dim);
        }
        return dimension;
    }

    public int getDimension() {
        return dimensionProperty().get();
    }

    public StringExpression axesDescriptionProperty() {
        return axesDescription;
    }

    public StringExpression wellKnownTextProperty() {
        return wkt;
    }

    public String getWellKnownText() {
        return wkt.get();
    }

    public static CRSInfo unsupported(String crsCode, String description, String reason) {
        return new CRSInfo(crsCode, description, reason);
    }

    public static CRSInfo fromAuthorityCode(@NonNull String authCode, boolean forceLongitudFirst) {
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(authCode, forceLongitudFirst);
        } catch (NoSuchAuthorityCodeException notfound) {
            NoSuchElementException ex = new NoSuchElementException(notfound.getMessage());
            ex.initCause(notfound);
            throw ex;
        } catch (FactoryException unsupported) {
            String description;
            try {
                description =
                        CRS.getAuthorityFactory(forceLongitudFirst)
                                .getDescriptionText(authCode)
                                .toString();
            } catch (FactoryRegistryException | FactoryException ignore) {
                description = null;
            }
            return CRSInfo.unsupported(authCode, description, unsupported.getMessage());
        }
        return new CRSInfo(authCode, crs);
    }

    public static enum CoordinateSystemType {
        ENGINEERING,
        PROJECTED,
        GEOCENTRIC,
        GEOGRAPHIC,
        IMAGE,
        TEMPORAL,
        VERTICAL,
        UNKNOWN
    }

    private ReadOnlyObjectProperty<CoordinateSystemType> coordinateSystemTypeProperty;

    public ReadOnlyObjectProperty<CoordinateSystemType> coordinateSystemTypeProperty() {
        if (coordinateSystemTypeProperty == null) {
            coordinateSystemTypeProperty =
                    new SimpleObjectProperty<>(
                            this, "Coordinate System Type", _getCoordinateSystemType());
        }
        return coordinateSystemTypeProperty;
    }

    public CoordinateSystemType getCoordinateSystemType() {
        return _getCoordinateSystemType();
    }

    public String getProjectionName() {
        CoordinateReferenceSystem crs = getCrs();
        if (crs instanceof ProjectedCRS) {
            Projection projection = ((ProjectedCRS) crs).getConversionFromBase();
            String projectionName = projection.getMethod().getName().getCode();
            return projectionName;
        }
        return null;
    }

    private CoordinateSystemType _getCoordinateSystemType() {
        CoordinateReferenceSystem baseCrs = getCrs();
        if (null == baseCrs) {
            return CoordinateSystemType.UNKNOWN;
        }
        if (baseCrs instanceof CompoundCRS) {
            baseCrs = ((CompoundCRS) baseCrs).getCoordinateReferenceSystems().get(0);
        }
        final CoordinateSystemType type;
        if (baseCrs instanceof EngineeringCRS) {
            type = CoordinateSystemType.ENGINEERING;
        } else if (baseCrs instanceof ProjectedCRS) {
            type = CoordinateSystemType.PROJECTED;
        } else if (baseCrs instanceof GeocentricCRS) {
            type = CoordinateSystemType.GEOCENTRIC;
        } else if (baseCrs instanceof GeographicCRS) {
            type = CoordinateSystemType.GEOGRAPHIC;
        } else if (baseCrs instanceof ImageCRS) {
            type = CoordinateSystemType.IMAGE;
        } else if (baseCrs instanceof TemporalCRS) {
            type = CoordinateSystemType.TEMPORAL;
        } else if (baseCrs instanceof VerticalCRS) {
            type = CoordinateSystemType.VERTICAL;
        } else {
            type = CoordinateSystemType.UNKNOWN;
        }
        return type;
    }
}
