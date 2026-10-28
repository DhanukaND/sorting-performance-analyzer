package com.sortinganalyzer.ui.controller;

import java.io.File;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WizardState {

    public static final class AlgorithmResult {
        private final String algorithmName;
        private final List<Long> runTimesNanos;
        private final double averageMs;
        private final double minMs;
        private final double maxMs;

        public AlgorithmResult(String algorithmName, List<Long> runTimesNanos,
                               double averageMs, double minMs, double maxMs) {
            this.algorithmName = algorithmName;
            this.runTimesNanos = List.copyOf(runTimesNanos);
            this.averageMs = averageMs;
            this.minMs = minMs;
            this.maxMs = maxMs;
        }

        public String getAlgorithmName() { return algorithmName; }
        public List<Long> getRunTimesNanos() { return runTimesNanos; }
        public double getAverageMs() { return averageMs; }
        public double getMinMs() { return minMs; }
        public double getMaxMs() { return maxMs; }
    }

    private static final WizardState INSTANCE = new WizardState();

    public static WizardState get() {
        return INSTANCE;
    }

    private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
    private final StringProperty selectedColumn = new SimpleStringProperty();
    private final IntegerProperty selectedColumnIndex = new SimpleIntegerProperty(-1);
    private final ObjectProperty<List<List<String>>> parsedRows = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<Double>> selectedColumnValues = new SimpleObjectProperty<>(List.of());
    private final IntegerProperty datasetSize = new SimpleIntegerProperty(0);
    private final IntegerProperty runCount = new SimpleIntegerProperty(3);
    private final ObjectProperty<List<AlgorithmResult>> algorithmResults = new SimpleObjectProperty<>(List.of());

    private WizardState() { }

    public ObjectProperty<File> selectedFileProperty() { return selectedFile; }
    public StringProperty selectedColumnProperty() { return selectedColumn; }
    public IntegerProperty selectedColumnIndexProperty() { return selectedColumnIndex; }
    public ObjectProperty<List<List<String>>> parsedRowsProperty() { return parsedRows; }
    public ObjectProperty<List<Double>> selectedColumnValuesProperty() { return selectedColumnValues; }
    public IntegerProperty datasetSizeProperty() { return datasetSize; }
    public IntegerProperty runCountProperty() { return runCount; }
    public ObjectProperty<List<AlgorithmResult>> algorithmResultsProperty() { return algorithmResults; }

    public void setParsedRows(List<List<String>> rows) {
        parsedRows.set(rows == null ? List.of() : List.copyOf(rows));
    }

    public void setSelectedColumnValues(List<Double> values) {
        selectedColumnValues.set(values == null ? List.of() : List.copyOf(values));
    }

    public void setAlgorithmResults(List<AlgorithmResult> results) {
        algorithmResults.set(results == null ? List.of() : List.copyOf(results));
    }

    public void reset() {
        selectedFile.set(null);
        selectedColumn.set(null);
        selectedColumnIndex.set(-1);
        parsedRows.set(List.of());
        selectedColumnValues.set(List.of());
        datasetSize.set(0);
        runCount.set(3);
        algorithmResults.set(List.of());
    }
}