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
import java.util.function.Supplier;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FileInputFieldSkin extends SkinBase<FileInputField> {

    /**
     * A button to open a {@link FileChooser} or {@link DirectoryChooser}, depending on the value of
     * {@link FileInputField#selectDirectoryProperty()}.
     *
     * <p>The chooser returned value is set as the value for {@link #text}
     */
    private final Button chooseButton;

    /**
     * The text field with the file or directory path, its value is bound to {@link
     * FileInputField#valueProperty()}
     */
    private final TextField text;

    protected FileInputFieldSkin(FileInputField control) {
        super(control);

        text = new TextField();
        chooseButton = new Button("...");
        HBox hbox = new HBox();
        hbox.getChildren().addAll(text, chooseButton);
        getChildren().add(hbox);
        chooseButton.setOnAction(this::chooseFile);

        ObjectProperty<File> valueProperty = getSkinnable().valueProperty();
        if (valueProperty.get() != null) {
            text.setText(valueProperty.get().getAbsolutePath());
        }

        ObjectBinding<File> valueBinding =
                Bindings.createObjectBinding(
                        () -> {
                            String value = text.getText();
                            if (value == null || value.trim().isEmpty()) {
                                return null;
                            }
                            return new File(value);
                        },
                        text.textProperty());

        getSkinnable().valueProperty().bind(valueBinding);
    }

    /**
     * Chooses a file and updates the value of the {@link #text} {@link TextField}, which in turn
     * will update the {@link FileInputField#valueProperty()}. This way the FileInputField value is
     * bound both to typed text as to the file chosen
     */
    private void chooseFile(ActionEvent e) {

        Window ownerWindow = null;
        StringProperty titleProperty;
        File file = getSkinnable().valueProperty().get();
        Supplier<File> callback;
        if (getSkinnable().selectDirectoryProperty().get()) {
            DirectoryChooser dchooser = new DirectoryChooser();
            titleProperty = dchooser.titleProperty();
            if (file != null && file.exists()) {
                dchooser.setInitialDirectory(file);
            }
            callback = () -> dchooser.showDialog(ownerWindow);
        } else {
            FileChooser fchooser = new FileChooser();
            titleProperty = fchooser.titleProperty();
            if (file != null && file.exists()) {
                fchooser.setInitialDirectory(file.getParentFile());
                fchooser.setInitialFileName(file.getAbsolutePath());
            }
            callback = () -> fchooser.showOpenDialog(ownerWindow);
        }
        String title = getSkinnable().titleProperty().get();
        if (null != title) {
            titleProperty.set(title);
        }

        file = callback.get();
        if (file != null) {
            text.setText(file.getAbsolutePath());
        }
    }
}
