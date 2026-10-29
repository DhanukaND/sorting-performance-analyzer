package com.sortinganalyzer.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Step 4: displays the results produced on the Run Algorithms step.
 * Rows are ordered from fastest to slowest average time, and one "Run N"
 * column is created for each run that was executed.
 */
public class ResultsController {

    @FXML private Label bestAlgorithmNameLabel;
    @FXML private Label bestAlgorithmTimeLabel;
    @FXML private TableView<ResultRow> performanceTable;
    @FXML private BarChart<String, Number> performanceChart;

    @FXML private TableColumn<ResultRow, String> algoNameColumn;
    @FXML private TableColumn<ResultRow, ?> executionGroupColumn;
    @FXML private TableColumn<ResultRow, String> avgColumn;
    @FXML private TableColumn<ResultRow, String> minColumn;
    @FXML private TableColumn<ResultRow, String> maxColumn;

    @FXML
    private void initialize() {
        configureFixedColumn(algoNameColumn, ResultRow::algorithmName);
        configureFixedColumn(avgColumn, ResultRow::average);
        configureFixedColumn(minColumn, ResultRow::minimum);
        configureFixedColumn(maxColumn, ResultRow::maximum);

        List<WizardState.AlgorithmResult> stored = WizardState.get().algorithmResultsProperty().get();
        if (stored == null || stored.isEmpty()) {
            showEmpty();
            return;
        }

        // Fastest average first, so row 2 is the runner-up, row 3 the third best, and so on.
        List<WizardState.AlgorithmResult> sorted = new ArrayList<>(stored);
        sorted.sort(Comparator.comparingDouble(WizardState.AlgorithmResult::getAverageMs));

        int runs = sorted.stream()
                .mapToInt(r -> r.getRunTimesNanos().size())
                .max()
                .orElse(0);
        buildRunColumns(runs);

        List<ResultRow> rows = new ArrayList<>();
        for (WizardState.AlgorithmResult result : sorted) {
            List<String> runTexts = new ArrayList<>();
            for (int i = 0; i < runs; i++) {
                runTexts.add(i < result.getRunTimesNanos().size()
                        ? format(result.getRunTimesNanos().get(i) / 1_000_000.0)
                        : "—");
            }
            rows.add(new ResultRow(
                    result.getAlgorithmName(),
                    runTexts,
                    format(result.getAverageMs()),
                    format(result.getMinMs()),
                    format(result.getMaxMs())));
        }
        performanceTable.setItems(FXCollections.observableArrayList(rows));

        WizardState.AlgorithmResult best = sorted.get(0);
        bestAlgorithmNameLabel.setText(best.getAlgorithmName());
        bestAlgorithmTimeLabel.setText(format(best.getAverageMs()) + " ms");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average time");
        for (WizardState.AlgorithmResult result : sorted) {
            series.getData().add(new XYChart.Data<>(result.getAlgorithmName(), result.getAverageMs()));
        }
        performanceChart.getData().setAll(series);
    }

    /** Creates Run 1.N columns, followed by Average, Min and Max, under the group header. */
    private void buildRunColumns(int runs) {
        List<TableColumn<ResultRow, ?>> columns = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            final int runIndex = i;
            TableColumn<ResultRow, String> column = new TableColumn<>("Run " + (i + 1));
            column.setSortable(false);
            column.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().runs().get(runIndex)));
            columns.add(column);
        }
        columns.add(avgColumn);
        columns.add(minColumn);
        columns.add(maxColumn);
        executionGroupColumn.getColumns().setAll(columns);
    }

    private void configureFixedColumn(TableColumn<ResultRow, String> column,
                                      java.util.function.Function<ResultRow, String> getter) {
        column.setSortable(false);   // keep the fastest-to-slowest order fixed
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
    }

    private void showEmpty() {
        performanceTable.getItems().clear();
        performanceChart.getData().clear();
        bestAlgorithmNameLabel.setText("Unavailable");
        bestAlgorithmTimeLabel.setText("No results to show");
    }

    private static String format(double milliseconds) {
        return String.format(Locale.ROOT, "%.3f", milliseconds);
    }

    private record ResultRow(String algorithmName, List<String> runs,
                             String average, String minimum, String maximum) { }
}