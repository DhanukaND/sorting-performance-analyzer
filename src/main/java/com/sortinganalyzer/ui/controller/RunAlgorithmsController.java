package com.sortinganalyzer.ui.controller;

import com.sortinganalyzer.sorting.HeapSort;
import com.sortinganalyzer.sorting.InsertionSort;
import com.sortinganalyzer.sorting.MergeSort;
import com.sortinganalyzer.sorting.QuickSort;
import com.sortinganalyzer.sorting.ShellSort;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class RunAlgorithmsController implements WizardStep {

    private static final int DEFAULT_RUNS = 3;
    private static final int MAX_RUNS = 10;

    @FXML private VBox mergeCard;
    @FXML private VBox quickCard;
    @FXML private VBox insertionCard;
    @FXML private VBox shellCard;
    @FXML private VBox heapCard;

    @FXML private CheckBox mergeCheck;
    @FXML private CheckBox quickCheck;
    @FXML private CheckBox insertionCheck;
    @FXML private CheckBox shellCheck;
    @FXML private CheckBox heapCheck;

    @FXML private Spinner<Integer> dataSizeSpinner;
    @FXML private Spinner<Integer> runCountSpinner;
    @FXML private Label dataSizeHint;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;

    private final BooleanProperty hasSelection = new SimpleBooleanProperty(false);
    private Runnable navigationCallback;

    @FXML
    private void initialize() {
        configureSpinner(dataSizeSpinner, 1, 1, 1);
        configureSpinner(runCountSpinner, DEFAULT_RUNS, 1, MAX_RUNS);

        bindCardSelection(mergeCard, mergeCheck);
        bindCardSelection(quickCard, quickCheck);
        bindCardSelection(insertionCard, insertionCheck);
        bindCardSelection(shellCard, shellCheck);
        bindCardSelection(heapCard, heapCheck);

        applyDefaultSelection();
        refreshDataSizeBounds();
        syncSelectionState();

        // Keep the data size bounds in sync if the selected column changes later.
        WizardState.get().selectedColumnValuesProperty()
                .addListener((obs, oldValue, newValue) -> refreshDataSizeBounds());
    }

    // ===== WizardStep =====

    @Override
    public ObservableBooleanValue canProceed() {
        return hasSelection;
    }

    @Override
    public String getNextText() {
        return "Run Algorithms";
    }

    /**
     * Starts the analysis in the background. Returns false so the wizard does not
     * advance by itself; navigation happens through the callback once results exist.
     */
    @Override
    public boolean onNext() {
        runAnalysis();
        return false;
    }

    public void setNavigationCallback(Runnable callback) {
        this.navigationCallback = callback;
    }

    /** Call this each time the step is shown, in case the selected column changed. */
    public void refreshDataSizeBounds() {
        List<Double> values = WizardState.get().selectedColumnValuesProperty().get();
        int totalRows = values == null ? 0 : values.size();
        int max = Math.max(1, totalRows);

        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, max);
        factory.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                WizardState.get().datasetSizeProperty().set(newValue);
            }
        });
        dataSizeSpinner.setValueFactory(factory);

        dataSizeHint.setText("Total rows: " + totalRows);
        WizardState.get().datasetSizeProperty().set(factory.getValue());
    }

    // ===== Card toggling =====

    @FXML
    private void toggleMergeCard(MouseEvent event) {
        mergeCheck.setSelected(!mergeCheck.isSelected());
    }

    @FXML
    private void toggleQuickCard(MouseEvent event) {
        quickCheck.setSelected(!quickCheck.isSelected());
    }

    @FXML
    private void toggleInsertionCard(MouseEvent event) {
        insertionCheck.setSelected(!insertionCheck.isSelected());
    }

    @FXML
    private void toggleShellCard(MouseEvent event) {
        shellCheck.setSelected(!shellCheck.isSelected());
    }

    @FXML
    private void toggleHeapCard(MouseEvent event) {
        heapCheck.setSelected(!heapCheck.isSelected());
    }

    private void bindCardSelection(VBox card, CheckBox checkBox) {
        checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            card.getStyleClass().remove("algorithm-card-selected");
            if (Boolean.TRUE.equals(newValue)) {
                card.getStyleClass().add("algorithm-card-selected");
            }
            syncSelectionState();
        });
    }

    private void syncSelectionState() {
        hasSelection.set(
                mergeCheck.isSelected() || quickCheck.isSelected() || insertionCheck.isSelected()
                        || shellCheck.isSelected() || heapCheck.isSelected()
        );
    }

    private void applyDefaultSelection() {
        mergeCheck.setSelected(true);
        quickCheck.setSelected(true);
        insertionCheck.setSelected(true);
        shellCheck.setSelected(true);
        heapCheck.setSelected(true);
    }

    // ===== Spinners =====

    private void configureSpinner(Spinner<Integer> spinner, int defaultValue, int min, int max) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, defaultValue));
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitSpinner(spinner);
            }
        });
    }

    /** Applies the text typed in the editor to the value factory, clamped to its bounds. */
    private void commitSpinner(Spinner<Integer> spinner) {
        if (!(spinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory factory)) {
            return;
        }
        try {
            int typed = Integer.parseInt(spinner.getEditor().getText().trim());
            factory.setValue(Math.max(factory.getMin(), Math.min(factory.getMax(), typed)));
        } catch (NumberFormatException ignored) {
            // keep the previous valid value
        }
        spinner.getEditor().setText(String.valueOf(factory.getValue()));
    }

    // ===== Analysis =====

    private void runAnalysis() {
        hideError();

        commitSpinner(dataSizeSpinner);
        commitSpinner(runCountSpinner);

        if (!hasSelection.get()) {
            showError("Select at least one algorithm to run.");
            return;
        }

        List<Double> columnValues = WizardState.get().selectedColumnValuesProperty().get();
        if (columnValues == null || columnValues.isEmpty()) {
            showError("No numeric values are available for the selected column.");
            return;
        }

        int totalRows = columnValues.size();
        int dataSize = dataSizeSpinner.getValue() == null ? totalRows : dataSizeSpinner.getValue();
        if (dataSize < 1 || dataSize > totalRows) {
            showError("Data size must be between 1 and the total rows (" + totalRows + ").");
            return;
        }

        int runs = runCountSpinner.getValue() == null ? DEFAULT_RUNS : runCountSpinner.getValue();
        if (runs < 1 || runs > MAX_RUNS) {
            showError("Number of runs must be between 1 and " + MAX_RUNS + ".");
            return;
        }

        List<Double> sourceData = new ArrayList<>(columnValues.subList(0, dataSize));

        List<AlgorithmSpec> selected = collectSelectedAlgorithms();
        if (selected.isEmpty()) {
            showError("Select at least one algorithm to run.");
            return;
        }

        progressIndicator.setVisible(true);
        setControlsDisabled(true);

        Task<List<WizardState.AlgorithmResult>> task = new Task<>() {
            @Override
            protected List<WizardState.AlgorithmResult> call() {
                List<WizardState.AlgorithmResult> localResults = new ArrayList<>();
                for (AlgorithmSpec spec : selected) {
                    // Warm-up on a throwaway copy so the JIT is ready and timed runs get unsorted data.
                    spec.sorter().sort(new ArrayList<>(sourceData));

                    List<Long> runTimesNanos = new ArrayList<>();
                    for (int i = 0; i < runs; i++) {
                        List<Double> freshData = new ArrayList<>(sourceData);
                        long start = System.nanoTime();
                        spec.sorter().sort(freshData);
                        runTimesNanos.add(System.nanoTime() - start);
                    }

                    long average = (long) runTimesNanos.stream().mapToLong(Long::longValue).average().orElse(0L);
                    long min = runTimesNanos.stream().mapToLong(Long::longValue).min().orElse(0L);
                    long max = runTimesNanos.stream().mapToLong(Long::longValue).max().orElse(0L);
                    localResults.add(new WizardState.AlgorithmResult(
                            spec.name(),
                            runTimesNanos,
                            nanosToMs(average),
                            nanosToMs(min),
                            nanosToMs(max)
                    ));
                }
                return localResults;
            }
        };

        task.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            setControlsDisabled(false);
            WizardState.get().setAlgorithmResults(task.getValue());
            WizardState.get().datasetSizeProperty().set(dataSize);
            WizardState.get().runCountProperty().set(runs);
            if (navigationCallback != null) {
                navigationCallback.run();
            }
        });

        task.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            setControlsDisabled(false);
            Throwable error = task.getException();
            showError(error != null && error.getMessage() != null
                    ? error.getMessage()
                    : "Sorting analysis failed.");
        });

        Thread thread = new Thread(task, "run-algorithms-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void setControlsDisabled(boolean disabled) {
        dataSizeSpinner.setDisable(disabled);
        runCountSpinner.setDisable(disabled);
        mergeCard.setDisable(disabled);
        quickCard.setDisable(disabled);
        insertionCard.setDisable(disabled);
        shellCard.setDisable(disabled);
        heapCard.setDisable(disabled);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private List<AlgorithmSpec> collectSelectedAlgorithms() {
        List<AlgorithmSpec> selected = new ArrayList<>();
        if (mergeCheck.isSelected()) selected.add(new AlgorithmSpec("Merge Sort", values -> sortWith(new MergeSort(), values)));
        if (quickCheck.isSelected()) selected.add(new AlgorithmSpec("Quick Sort", values -> sortWith(new QuickSort(), values)));
        if (insertionCheck.isSelected()) selected.add(new AlgorithmSpec("Insertion Sort", values -> sortWith(new InsertionSort(), values)));
        if (shellCheck.isSelected()) selected.add(new AlgorithmSpec("Shell Sort", values -> sortWith(new ShellSort(), values)));
        if (heapCheck.isSelected()) selected.add(new AlgorithmSpec("Heap Sort", values -> sortWith(new HeapSort(), values)));
        return selected;
    }

    private static void sortWith(Object sorter, List<Double> values) {
        try {
            double[] array = values.stream().mapToDouble(Double::doubleValue).toArray();
            if (sorter instanceof MergeSort mergeSort) {
                mergeSort.sort(array);
            } else if (sorter instanceof QuickSort quickSort) {
                quickSort.sort(array);
            } else if (sorter instanceof InsertionSort insertionSort) {
                insertionSort.sort(array);
            } else if (sorter instanceof ShellSort shellSort) {
                shellSort.sort(array);
            } else if (sorter instanceof HeapSort heapSort) {
                heapSort.sort(array);
            }
            for (int i = 0; i < array.length; i++) {
                values.set(i, array[i]);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sort data with " + sorter.getClass().getSimpleName(), e);
        }
    }

    private static double nanosToMs(long nanos) {
        return nanos / 1_000_000.0;
    }

    private interface DataSorter {
        void sort(List<Double> values);
    }

    private record AlgorithmSpec(String name, DataSorter sorter) { }
}