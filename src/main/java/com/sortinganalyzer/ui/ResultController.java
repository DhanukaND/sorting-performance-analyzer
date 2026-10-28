package com.sortinganalyzer.ui;

import com.sortinganalyzer.csv.CsvLoader;
import com.sortinganalyzer.sorting.HeapSort;
import com.sortinganalyzer.sorting.InsertionSort;
import com.sortinganalyzer.sorting.MergeSort;
import com.sortinganalyzer.sorting.QuickSort;
import com.sortinganalyzer.sorting.ShellSort;
import com.sortinganalyzer.ui.controller.WizardState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controls the Step 4 results view and evaluates the configured sorting
 * algorithms against the selected numeric column.
 *
 */
public class ResultController {

    private static final int RUN_COUNT = 3;
    private static final String TIME_FORMAT = "%.2f ms";

    @FXML
    private Label bestAlgorithmNameLabel;

    @FXML
    private Label bestAlgorithmTimeLabel;

    @FXML
    private TableView<AlgorithmResult> performanceTable;

    @FXML
    private BarChart<String, Number> performanceChart;

    @FXML
    private TableColumn<AlgorithmResult, String> algoNameColumn;

    @FXML
    private TableColumn<AlgorithmResult, String> run1Column;

    @FXML
    private TableColumn<AlgorithmResult, String> run2Column;

    @FXML
    private TableColumn<AlgorithmResult, String> run3Column;

    @FXML
    private TableColumn<AlgorithmResult, String> avgColumn;

    @FXML
    private TableColumn<AlgorithmResult, String> minColumn;

    @FXML
    private TableColumn<AlgorithmResult, String> maxColumn;

    private int selectedRunCount = 3;

    private final List<AlgorithmDefinition> algorithms = List.of(
            new AlgorithmDefinition("Merge Sort", new MergeSort()::sort),
            new AlgorithmDefinition("Quick Sort", new QuickSort()::sort),
            new AlgorithmDefinition("Insertion Sort", new InsertionSort()::sort),
            new AlgorithmDefinition("Shell Sort", new ShellSort()::sort),
            new AlgorithmDefinition("Heap Sort", new HeapSort()::sort)
    );

    /**
     * Configures the table columns after the FXML view has been loaded.
     */
    @FXML
    private void initialize() {
        configureColumn(algoNameColumn, "algorithmName");
        configureColumn(run1Column, "run1");
        configureColumn(run2Column, "run2");
        configureColumn(run3Column, "run3");
        configureColumn(avgColumn, "average");
        configureColumn(minColumn, "minimum");
        configureColumn(maxColumn, "maximum");
        selectedRunCount = Math.max(1, Math.min(RUN_COUNT, WizardState.get().runCountProperty().get()));
        updateRunColumns();
        try {
            double[] dataset = loadSelectedDataset();
            evaluatePerformance(dataset, selectedRunCount);
        } catch (IOException | IllegalArgumentException exception) {
            showLoadError(exception.getMessage());
        }
    }

    /**
     * Benchmarks all supported algorithms three times and updates the results
     * table and fastest-algorithm summary.
     *
     * @param dataset the numeric column to sort; it is never modified
     * @throws IllegalArgumentException if {@code dataset} is {@code null}
     */
    public void evaluatePerformance(double[] dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null.");
        }

        evaluatePerformance(dataset, selectedRunCount);
    }

    /**
     * Evaluates the dataset using the requested number of benchmark runs.
     *
     * @param dataset the numeric column to sort; it is never modified
     * @param runCount number of runs, from one through three
     */
    public void evaluatePerformance(double[] dataset, int runCount) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null.");
        }
        if (runCount < 1 || runCount > RUN_COUNT) {
            throw new IllegalArgumentException("Run count must be between 1 and 3.");
        }
        selectedRunCount = runCount;
        updateRunColumns();

        ObservableList<AlgorithmResult> results = FXCollections.observableArrayList();
        AlgorithmResult bestResult = null;
        List<WizardState.AlgorithmResult> sharedResults = new ArrayList<>();

        for (AlgorithmDefinition algorithm : algorithms) {
            double[] runTimes = new double[RUN_COUNT];
            List<Long> runTimesNanos = new ArrayList<>(selectedRunCount);

            for (int run = 0; run < selectedRunCount; run++) {
                double[] input = Arrays.copyOf(dataset, dataset.length);
                long start = System.nanoTime();
                algorithm.sort(input);
                long elapsedNanos = System.nanoTime() - start;
                runTimesNanos.add(elapsedNanos);
                runTimes[run] = elapsedNanos / 1_000_000.0;
            }

            AlgorithmResult result = new AlgorithmResult(
                    algorithm.name(),
                    runTimes[0],
                    runTimes[1],
                    runTimes[2],
                    selectedRunCount
            );
            results.add(result);
            sharedResults.add(new WizardState.AlgorithmResult(
                    algorithm.name(),
                    runTimesNanos,
                    result.averageValue(),
                    result.minimumValue(),
                    result.maximumValue()
            ));

            if (bestResult == null || result.averageValue() < bestResult.averageValue()) {
                bestResult = result;
            }
        }

        if (performanceTable != null) {
            performanceTable.setItems(results);
        }
        WizardState.get().setAlgorithmResults(sharedResults);
        updateChart(results);
        if (bestResult != null) {
            if (bestAlgorithmNameLabel != null) {
                bestAlgorithmNameLabel.setText(bestResult.algorithmName());
            }
            if (bestAlgorithmTimeLabel != null) {
                bestAlgorithmTimeLabel.setText(bestResult.average());
            }
        }
    }

    private double[] loadSelectedDataset() throws IOException {
        List<Double> selectedValues = WizardState.get().selectedColumnValuesProperty().get();
        if (selectedValues != null && !selectedValues.isEmpty()) {
            double[] dataset = new double[selectedValues.size()];
            for (int index = 0; index < selectedValues.size(); index++) {
                Double value = selectedValues.get(index);
                if (value == null || !Double.isFinite(value)) {
                    throw new IllegalArgumentException("The selected column contains invalid numeric data.");
                }
                dataset[index] = value;
            }
            return dataset;
        }

        return CsvLoader.loadNumericColumn(
                WizardState.get().selectedFileProperty().get(),
                WizardState.get().selectedColumnIndexProperty().get());
    }

    private void updateRunColumns() {
        if (run1Column != null) run1Column.setVisible(selectedRunCount >= 1);
        if (run2Column != null) run2Column.setVisible(selectedRunCount >= 2);
        if (run3Column != null) run3Column.setVisible(selectedRunCount >= 3);
    }

    private void updateChart(ObservableList<AlgorithmResult> results) {
        if (performanceChart == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average time");
        for (AlgorithmResult result : results) {
            series.getData().add(new XYChart.Data<>(result.getAlgorithmName(), result.averageValue()));
        }
        performanceChart.getData().setAll(series);
    }

    private void showLoadError(String message) {
        if (performanceTable != null) {
            performanceTable.getItems().clear();
        }
        if (performanceChart != null) {
            performanceChart.getData().clear();
        }
        if (bestAlgorithmNameLabel != null) {
            bestAlgorithmNameLabel.setText("Unavailable");
        }
        if (bestAlgorithmTimeLabel != null) {
            bestAlgorithmTimeLabel.setText(message == null ? "Unable to load dataset" : message);
        }
    }

    private <T> void configureColumn(TableColumn<AlgorithmResult, T> column, String property) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(property));
        }
    }

    private record AlgorithmDefinition(String name, Consumer<double[]> sorter) {
        private void sort(double[] values) {
            sorter.accept(values);
        }
    }

    /**
     * Immutable display row containing the three timings and their summary.
     */
    public static final class AlgorithmResult {
        private final String algorithmName;
        private final String run1;
        private final String run2;
        private final String run3;
        private final String average;
        private final String minimum;
        private final String maximum;
        private final double averageValue;
        private final double minimumValue;
        private final double maximumValue;

        private AlgorithmResult(String algorithmName, double run1, double run2, double run3, int runCount) {
            this.algorithmName = algorithmName;
            this.run1 = format(run1);
            this.run2 = runCount >= 2 ? format(run2) : "—";
            this.run3 = runCount >= 3 ? format(run3) : "—";
            double total = run1 + (runCount >= 2 ? run2 : 0) + (runCount >= 3 ? run3 : 0);
            this.averageValue = total / runCount;
            this.average = format(averageValue);
            minimumValue = runCount == 1 ? run1
                    : runCount == 2 ? Math.min(run1, run2) : Math.min(run1, Math.min(run2, run3));
            maximumValue = runCount == 1 ? run1
                    : runCount == 2 ? Math.max(run1, run2) : Math.max(run1, Math.max(run2, run3));
            this.minimum = format(minimumValue);
            this.maximum = format(maximumValue);
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public String getRun1() {
            return run1;
        }

        public String getRun2() {
            return run2;
        }

        public String getRun3() {
            return run3;
        }

        public String getAverage() {
            return average;
        }

        public String getMinimum() {
            return minimum;
        }

        public String getMaximum() {
            return maximum;
        }

        private String algorithmName() {
            return algorithmName;
        }

        private String average() {
            return average;
        }

        private double averageValue() {
            return averageValue;
        }

        private double minimumValue() {
            return minimumValue;
        }

        private double maximumValue() {
            return maximumValue;
        }

        private static String format(double milliseconds) {
            return String.format(java.util.Locale.ROOT, TIME_FORMAT, milliseconds);
        }

    }
}
