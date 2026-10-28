package com.sortinganalyzer.ui.controller;

import javafx.beans.property.*;

import java.io.File;

public class WizardState {

    private static final WizardState INSTANCE = new WizardState();

    public static WizardState get() {
        return INSTANCE;
    }

    private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
    private final StringProperty selectedColumn = new SimpleStringProperty();
    private final IntegerProperty selectedColumnIndex = new SimpleIntegerProperty(-1);
    private final IntegerProperty runCount = new SimpleIntegerProperty(3);

    private WizardState() { }

    public ObjectProperty<File> selectedFileProperty() { return selectedFile; }
    public StringProperty selectedColumnProperty() { return selectedColumn; }
    public IntegerProperty selectedColumnIndexProperty() { return selectedColumnIndex; }
    public IntegerProperty runCountProperty() { return runCount; }

    /** Clear everything, e.g. for a "start over" action. */
    public void reset() {
        selectedFile.set(null);
        selectedColumn.set(null);
        selectedColumnIndex.set(-1);
        runCount.set(3);
    }
}