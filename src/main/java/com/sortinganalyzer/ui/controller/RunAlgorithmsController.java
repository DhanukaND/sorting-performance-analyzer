package com.sortinganalyzer.ui.controller;

import com.sortinganalyzer.sorting.HeapSort;
import com.sortinganalyzer.sorting.InsertionSort;
import com.sortinganalyzer.sorting.MergeSort;
import com.sortinganalyzer.sorting.QuickSort;
import com.sortinganalyzer.sorting.ShellSort;
import com.sortinganalyzer.sorting.Sorter;
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

    private static final int MAX_RUNS = 5;

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
        WizardState state = WizardState.get();

        // Restore the previously saved settings (defaults on first visit).
        int savedRuns = Math.max(1, Math.min(MAX_RUNS, state.runCountProperty().get()));
        configureSpinner(dataSizeSpinner, 1, 1, 1);
        configureSpinner(runCountSpinner, savedRuns, 1, MAX_RUNS);
        runCountSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                state.runCountProperty().set(newValue);
            }
        });
        state.runCountProperty().set(savedRuns);

        restoreAlgorithmSelection();

        // Listeners are attached after restoring so the restore itself doesn't overwrite the saved state.
        bindCardSelection(mergeCard, mergeCheck);
        bindCardSelection(quickCard, quickCheck);
        bindCardSelection(insertionCard, insertionCheck);
        bindCardSelection(shellCard, shellCheck);
        bindCardSelection(heapCard, heapCheck);

        refreshDataSizeBounds();
        syncSelectionState();

        state.selectedColumnValuesProperty()
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

    @Override
    public boolean onNext() {
        runAnalysis();
        return false;
    }

    public void setNavigationCallback(Runnable callback) {
        this.navigationCallback = callback;
    }

    /** Rebuilds the data size spinner, keeping the saved size when it is still valid. */
    public void refreshDataSizeBounds() {
        List<Double> values = WizardState.get().selectedColumnValuesProperty().get();
        int totalRows = values == null ? 0 : values.size();
        int max = Math.max(1, totalRows);

        int saved = WizardState.get().datasetSizeProperty().get();
        int initial = (saved >= 1 && saved <= max) ? saved : max;

        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, initial);
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

    @FXML private void toggleMergeCard(MouseEvent event)     { mergeCheck.setSelected(!mergeCheck.isSelected()); }
    @FXML private void toggleQuickCard(MouseEvent event)     { quickCheck.setSelected(!quickCheck.isSelected()); }
    @FXML private void toggleInsertionCard(MouseEvent event) { insertionCheck.setSelected(!insertionCheck.isSelected()); }
    @FXML private void toggleShellCard(MouseEvent event)     { shellCheck.setSelected(!shellCheck.isSelected()); }
    @FXML private void toggleHeapCard(MouseEvent event)      { heapCheck.setSelected(!heapCheck.isSelected()); }

    private void restoreAlgorithmSelection() {
        List<String> saved = WizardState.get().selectedAlgorithmsProperty().get();
        mergeCheck.setSelected(saved.contains(WizardState.MERGE_SORT));
        quickCheck.setSelected(saved.contains(WizardState.QUICK_SORT));
        insertionCheck.setSelected(saved.contains(WizardState.INSERTION_SORT));
        shellCheck.setSelected(saved.contains(WizardState.SHELL_SORT));
        heapCheck.setSelected(saved.contains(WizardState.HEAP_SORT));
    }

    private void bindCardSelection(VBox card, CheckBox checkBox) {
        updateCardStyle(card, checkBox.isSelected());
        checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            updateCardStyle(card, Boolean.TRUE.equals(newValue));
            syncSelectionState();
        });
    }

    private void updateCardStyle(VBox card, boolean selected) {
        card.getStyleClass().remove("algorithm-card-selected");
        if (selected) {
            card.getStyleClass().add("algorithm-card-selected");
        }
    }

    /** Updates the Next button state and saves the current selection. */
    private void syncSelectionState() {
        List<String> names = new ArrayList<>();
        if (mergeCheck.isSelected()) names.add(WizardState.MERGE_SORT);
        if (quickCheck.isSelected()) names.add(WizardState.QUICK_SORT);
        if (insertionCheck.isSelected()) names.add(WizardState.INSERTION_SORT);
        if (shellCheck.isSelected()) names.add(WizardState.SHELL_SORT);
        if (heapCheck.isSelected()) names.add(WizardState.HEAP_SORT);

        WizardState.get().setSelectedAlgorithms(names);
        hasSelection.set(!names.isEmpty());
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

        int runs = runCountSpinner.getValue() == null ? WizardState.DEFAULT_RUNS : runCountSpinner.getValue();
        if (runs < 1 || runs > MAX_RUNS) {
            showError("Number of runs must be between 1 and " + MAX_RUNS + ".");
            return;
        }

        List<AlgorithmSpec> selected = collectSelectedAlgorithms();
        if (selected.isEmpty()) {
            showError("Select at least one algorithm to run.");
            return;
        }

        // Primitive source array, built once; every run sorts its own fresh clone.
        double[] source = new double[dataSize];
        for (int i = 0; i < dataSize; i++) {
            source[i] = columnValues.get(i);
        }

        progressIndicator.setVisible(true);
        setControlsDisabled(true);

        Task<List<WizardState.AlgorithmResult>> task = new Task<>() {
            @Override
            protected List<WizardState.AlgorithmResult> call() {
                List<WizardState.AlgorithmResult> results = new ArrayList<>();
                for (AlgorithmSpec spec : selected) {
                    // Untimed warm-up so JIT compilation doesn't distort the first measured run.
                    spec.sorter().sort(source.clone());

                    List<Long> runTimesNanos = new ArrayList<>();
                    for (int i = 0; i < runs; i++) {
                        double[] data = source.clone();   // copy happens outside the timed section
                        long start = System.nanoTime();
                        spec.sorter().sort(data);
                        runTimesNanos.add(System.nanoTime() - start);
                    }

                    double average = runTimesNanos.stream().mapToLong(Long::longValue).average().orElse(0);
                    long min = runTimesNanos.stream().mapToLong(Long::longValue).min().orElse(0L);
                    long max = runTimesNanos.stream().mapToLong(Long::longValue).max().orElse(0L);

                    results.add(new WizardState.AlgorithmResult(
                            spec.name(), runTimesNanos,
                            nanosToMs(average), nanosToMs(min), nanosToMs(max)));
                }
                return results;
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

    private List<AlgorithmSpec> collectSelectedAlgorithms() {
        List<AlgorithmSpec> selected = new ArrayList<>();
        if (mergeCheck.isSelected()) selected.add(new AlgorithmSpec(WizardState.MERGE_SORT, new MergeSort()));
        if (quickCheck.isSelected()) selected.add(new AlgorithmSpec(WizardState.QUICK_SORT, new QuickSort()));
        if (insertionCheck.isSelected()) selected.add(new AlgorithmSpec(WizardState.INSERTION_SORT, new InsertionSort()));
        if (shellCheck.isSelected()) selected.add(new AlgorithmSpec(WizardState.SHELL_SORT, new ShellSort()));
        if (heapCheck.isSelected()) selected.add(new AlgorithmSpec(WizardState.HEAP_SORT, new HeapSort()));
        return selected;
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

    private static double nanosToMs(double nanos) {
        return nanos / 1_000_000.0;
    }

    private record AlgorithmSpec(String name, Sorter sorter) { }
}