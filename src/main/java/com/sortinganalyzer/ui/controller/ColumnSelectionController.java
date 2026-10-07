package com.sortinganalyzer.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ColumnSelectionController implements WizardStep {

    private static final int PREVIEW_ROWS = 100;
    private static final String DEFAULT_PROMPT = "Choose a column";

    @FXML private ComboBox<String> columnBox;
    @FXML private Label previewSubtitle;
    @FXML private Label datasetSizeLabel;
    @FXML private TableView<ObservableList<String>> previewTable;

    /** Combo position -> real CSV column index (the combo only lists numeric columns). */
    private final List<Integer> numericColumnIndexes = new ArrayList<>();

    @FXML
    private void initialize() {
        File csv = WizardState.get().selectedFileProperty().get();
        if (csv != null) {
            loadPreview(csv, PREVIEW_ROWS);
        }

        // remember choices (attached after loadPreview so clearing doesn't write null)
        columnBox.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            int pos = columnBox.getSelectionModel().getSelectedIndex();
            WizardState.get().selectedColumnProperty().set(value);
            WizardState.get().selectedColumnIndexProperty()
                    .set(pos >= 0 ? numericColumnIndexes.get(pos) : -1);
        });

        // restore the previous choice: the saved value is a CSV index, find its combo position
        int savedIndex = WizardState.get().selectedColumnIndexProperty().get();
        int pos = numericColumnIndexes.indexOf(savedIndex);
        if (pos >= 0) {
            columnBox.getSelectionModel().select(pos);
        }
    }

    // ---------- WizardStep ----------

    @Override
    public ObservableBooleanValue canProceed() {
        return columnBox.getSelectionModel().selectedItemProperty().isNotNull();
    }

    // ---------- CSV loading ----------

    private void loadPreview(File csv, int maxRows) {
        clearPreview();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setIgnoreEmptyLines(true)
                .build();

        try (Reader in = Files.newBufferedReader(csv.toPath());
             CSVParser parser = format.parse(in)) {

            Iterator<CSVRecord> records = parser.iterator();
            if (!records.hasNext()) {
                previewSubtitle.setText("File is empty");
                return;
            }

            // first record = header row
            List<String> headers = new ArrayList<>(records.next().toList());
            if (!headers.isEmpty()) {
                // strip a UTF-8 BOM that Excel adds to the first header
                headers.set(0, headers.get(0).replace("\uFEFF", ""));
            }

            List<String> titles = new ArrayList<>();
            for (int i = 0; i < headers.size(); i++) {
                String t = headers.get(i).trim();
                titles.add(t.isEmpty() ? "Column " + (i + 1) : t);
            }

            // preview table shows every column
            for (int i = 0; i < titles.size(); i++) {
                final int col = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(titles.get(i));
                column.setSortable(false);
                column.setCellValueFactory(cell -> new SimpleStringProperty(
                        col < cell.getValue().size() ? cell.getValue().get(col) : ""));
                previewTable.getColumns().add(column);
            }

            // one pass: keep the first maxRows, count everything, test columns for numbers
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            long totalRows = 0;

            boolean[] numeric = new boolean[titles.size()];
            boolean[] hasValue = new boolean[titles.size()];
            Arrays.fill(numeric, true);
            int candidates = titles.size();   // columns still possibly numeric

            while (records.hasNext()) {
                CSVRecord record = records.next();

                if (totalRows < maxRows) {
                    rows.add(FXCollections.observableArrayList(record.toList()));
                }
                totalRows++;

                if (candidates > 0) {
                    for (int c = 0; c < numeric.length; c++) {
                        if (!numeric[c] || c >= record.size()) continue;

                        String v = record.get(c).trim();
                        if (v.isEmpty()) continue;          // blanks are ignored

                        if (isNumber(v)) {
                            hasValue[c] = true;
                        } else {
                            numeric[c] = false;             // never checked again
                            candidates--;
                        }
                    }
                }
            }
            previewTable.setItems(rows);

            // the combo lists only all-numeric columns
            List<String> numericTitles = new ArrayList<>();
            for (int c = 0; c < numeric.length; c++) {
                if (numeric[c] && hasValue[c]) {
                    numericColumnIndexes.add(c);
                    numericTitles.add(titles.get(c));
                }
            }
            columnBox.setItems(FXCollections.observableArrayList(numericTitles));

            if (numericTitles.isEmpty()) {
                columnBox.setPromptText("No numeric columns found");
                columnBox.setDisable(true);
            }

            int shown = rows.size();
            previewSubtitle.setText(shown < totalRows
                    ? "Showing first " + shown + " rows"
                    : "Showing all " + shown + " rows");
            datasetSizeLabel.setText(totalRows + " rows × " + headers.size() + " columns");

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            clearPreview();
            previewSubtitle.setText("Could not read the file");
        }
    }

    /** Fast check: optional sign, digits, optional decimal point, optional exponent. Input must be non-empty. */
    private static boolean isNumber(String s) {
        int n = s.length(), i = 0;
        char first = s.charAt(0);
        if (first == '+' || first == '-') i++;

        int digits = 0;
        boolean dot = false;
        for (; i < n; i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') digits++;
            else if (ch == '.' && !dot) dot = true;
            else break;
        }
        if (digits == 0) return false;
        if (i == n) return true;

        char e = s.charAt(i);
        if (e != 'e' && e != 'E') return false;
        i++;
        if (i < n && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;

        int expDigits = 0;
        for (; i < n; i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') expDigits++;
            else return false;
        }
        return expDigits > 0;
    }

    private void clearPreview() {
        previewTable.getColumns().clear();
        previewTable.getItems().clear();
        previewSubtitle.setText("");
        datasetSizeLabel.setText("");
        columnBox.getItems().clear();
        columnBox.setDisable(false);
        columnBox.setPromptText(DEFAULT_PROMPT);
        numericColumnIndexes.clear();
    }
}