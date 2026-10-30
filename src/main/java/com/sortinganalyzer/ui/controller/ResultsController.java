package com.sortinganalyzer.ui.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

/**
 * Step 4: displays the results produced on the Run Algorithms step.
 * Rows are ordered from fastest to slowest average time, and one "Run N"
 * column is created for each run that was executed.
 */
public class ResultsController implements WizardStep {

    // table sizing: two header rows (32 each) + data rows + a little border
    private static final double TABLE_ROW_HEIGHT = 28;
    private static final double TABLE_CHROME = 68;

    // chart sizing: every algorithm gets the same slice of height, so bar thickness stays constant
    private static final double CHART_ROW_HEIGHT = 48;
    private static final double CHART_CHROME = 90;
    private static final double CHART_MIN_HEIGHT = 180;

    private static final String BEST_BAR_STYLE = "-fx-bar-fill: #0A9F5B;";

    @FXML private Label bestAlgorithmNameLabel;
    @FXML private Label bestAlgorithmTimeLabel;
    @FXML private TableView<ResultRow> performanceTable;
    @FXML private BarChart<Number, String> performanceChart;

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
        avgColumn.getStyleClass().add("avg-column");

        performanceTable.setFixedCellSize(TABLE_ROW_HEIGHT);
        // the first row is the fastest algorithm, so highlight it
        performanceTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ResultRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("best-row");
                if (!empty && item != null && getIndex() == 0) {
                    getStyleClass().add("best-row");
                }
            }
        });

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
        fitTableToRows(rows.size());

        WizardState.AlgorithmResult best = sorted.get(0);
        bestAlgorithmNameLabel.setText(best.getAlgorithmName());
        bestAlgorithmTimeLabel.setText(format(best.getAverageMs()) + " ms");

        buildChart(sorted);
    }

    // ---------- Chart ----------

    private void buildChart(List<WizardState.AlgorithmResult> sorted) {
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        double max = 0;

        for (int i = 0; i < sorted.size(); i++) {
            WizardState.AlgorithmResult result = sorted.get(i);
            max = Math.max(max, result.getAverageMs());

            XYChart.Data<Number, String> data =
                    new XYChart.Data<>(result.getAverageMs(), result.getAlgorithmName());
            decorateBar(data, format(result.getAverageMs()) + " ms", i == 0);
            series.getData().add(data);
        }

        configureValueAxis(max);
        sizeChartFor(sorted.size());
        performanceChart.getData().setAll(series);
    }

    /** Adds the "0.378 ms" label at the end of a bar and colours the best bar green. */
    private void decorateBar(XYChart.Data<Number, String> data, String text, boolean best) {
        data.nodeProperty().addListener((obs, oldNode, bar) -> {
            if (bar == null) return;

            if (best) bar.setStyle(BEST_BAR_STYLE);

            Text label = new Text(text);
            label.getStyleClass().add("bar-value-label");

            Runnable place = () -> {
                Bounds b = bar.getBoundsInParent();
                Bounds t = label.getLayoutBounds();
                label.setLayoutX(b.getMaxX() + 8);
                label.setLayoutY(b.getMinY() + b.getHeight() / 2 - t.getMinY() - t.getHeight() / 2);
            };

            // the bar gets its parent (the plot area) a moment after it is created
            bar.parentProperty().addListener((o, oldParent, parent) -> {
                if (parent instanceof Group group && !group.getChildren().contains(label)) {
                    group.getChildren().add(label);
                }
            });
            bar.boundsInParentProperty().addListener((o, oldB, newB) -> place.run());
            label.layoutBoundsProperty().addListener((o, oldB, newB) -> place.run());
        });
    }

    /** Fixed 0..upper range with ~20% headroom so the value label never gets clipped. */
    private void configureValueAxis(double max) {
        NumberAxis axis = (NumberAxis) performanceChart.getXAxis();
        axis.setAnimated(false);
        axis.setMinorTickVisible(false);

        if (max <= 0) {
            axis.setAutoRanging(true);
            return;
        }
        double step = niceStep(max);
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(Math.ceil(max * 1.2 / step) * step);
        axis.setTickUnit(step);
    }

    private static double niceStep(double max) {
        double rough = max / 5;
        double magnitude = Math.pow(10, Math.floor(Math.log10(rough)));
        double normalized = rough / magnitude;
        double nice = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10;
        return nice * magnitude;
    }

    private void sizeChartFor(int algorithmCount) {
        double height = Math.max(CHART_MIN_HEIGHT, CHART_CHROME + algorithmCount * CHART_ROW_HEIGHT);
        performanceChart.setMinHeight(height);
        performanceChart.setPrefHeight(height);
        performanceChart.setMaxHeight(height);
    }

    // ---------- Table ----------

    /** Creates Run 1..N columns, followed by Average, Min and Max, under the group header. */
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

    /** Table is exactly as tall as its header plus its rows, so no blank filler rows appear. */
    private void fitTableToRows(int rowCount) {
        double height = TABLE_CHROME + Math.max(rowCount, 1) * TABLE_ROW_HEIGHT;
        performanceTable.setMinHeight(height);
        performanceTable.setPrefHeight(height);
        performanceTable.setMaxHeight(height);
    }

    private void configureFixedColumn(TableColumn<ResultRow, String> column,
                                      Function<ResultRow, String> getter) {
        column.setSortable(false);   // keep the fastest-to-slowest order fixed
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
    }

    private void showEmpty() {
        performanceTable.getItems().clear();
        performanceChart.getData().clear();
        fitTableToRows(2);
        sizeChartFor(0);
        bestAlgorithmNameLabel.setText("Unavailable");
        bestAlgorithmTimeLabel.setText("No results to show");
    }

    private static String format(double milliseconds) {
        return String.format(Locale.ROOT, "%.3f", milliseconds);
    }

    // ---------- WizardStep ----------

    /** The last screen's button restarts the wizard instead of "finishing". */
    @Override
    public String getNextText() {
        return "New Analysis";
    }

    @Override
    public String getNextIcon() {
        return "fas-redo-alt";
    }

    private record ResultRow(String algorithmName, List<String> runs,
                             String average, String minimum, String maximum) { }
}