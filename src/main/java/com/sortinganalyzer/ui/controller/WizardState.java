package com.sortinganalyzer.ui.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class WizardState {

    private static final WizardState INSTANCE = new WizardState();

    public static WizardState get() {
        return INSTANCE;
    }

    private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
    private final StringProperty selectedColumn = new SimpleStringProperty();

    private WizardState() { }

    public ObjectProperty<File> selectedFileProperty() { return selectedFile; }
    public StringProperty selectedColumnProperty() { return selectedColumn; }

    /** Clear everything, e.g. for a "start over" action. */
    public void reset() {
        selectedFile.set(null);
        selectedColumn.set(null);
    }
}