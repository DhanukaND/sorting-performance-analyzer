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

    public static final String MERGE_SORT = "Merge Sort";
    public static final String QUICK_SORT = "Quick Sort";
    public static final String INSERTION_SORT = "Insertion Sort";
    public static final String SHELL_SORT = "Shell Sort";
    public static final String HEAP_SORT = "Heap Sort";

    public static final List<String> ALL_ALGORITHMS =
            List.of(MERGE_SORT, QUICK_SORT, INSERTION_SORT, SHELL_SORT, HEAP_SORT);

    public static final int DEFAULT_RUNS = 3;

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
    private final ObjectProperty<List<Double>> selectedColumnValues = new SimpleObjectProperty<>(List.of());
    private final IntegerProperty datasetSize = new SimpleIntegerProperty(0);
    private final IntegerProperty runCount = new SimpleIntegerProperty(DEFAULT_RUNS);
    private final ObjectProperty<List<String>> selectedAlgorithms = new SimpleObjectProperty<>(ALL_ALGORITHMS);
    private final ObjectProperty<List<AlgorithmResult>> algorithmResults = new SimpleObjectProperty<>(List.of());

    private WizardState() { }

    public ObjectProperty<File> selectedFileProperty() { return selectedFile; }
    public StringProperty selectedColumnProperty() { return selectedColumn; }
    public IntegerProperty selectedColumnIndexProperty() { return selectedColumnIndex; }
    public ObjectProperty<List<Double>> selectedColumnValuesProperty() { return selectedColumnValues; }
    public IntegerProperty datasetSizeProperty() { return datasetSize; }
    public IntegerProperty runCountProperty() { return runCount; }
    public ObjectProperty<List<String>> selectedAlgorithmsProperty() { return selectedAlgorithms; }
    public ObjectProperty<List<AlgorithmResult>> algorithmResultsProperty() { return algorithmResults; }

    public void setSelectedColumnValues(List<Double> values) {
        selectedColumnValues.set(values == null ? List.of() : List.copyOf(values));
    }

    public void setSelectedAlgorithms(List<String> names) {
        selectedAlgorithms.set(names == null ? List.of() : List.copyOf(names));
    }

    public void setAlgorithmResults(List<AlgorithmResult> results) {
        algorithmResults.set(results == null ? List.of() : List.copyOf(results));
    }

}