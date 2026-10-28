package com.sortinganalyzer.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class CsvLoader {

    private CsvLoader() {
    }

    public static double[] loadNumericColumn(File csv, int columnIndex) throws IOException {
        if (csv == null || !csv.isFile()) {
            throw new IllegalArgumentException("CSV file does not exist.");
        }
        if (columnIndex < 0) {
            throw new IllegalArgumentException("A valid CSV column must be selected.");
        }

        List<Double> values = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setIgnoreEmptyLines(true)
                .build();

        try (Reader reader = Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            if (!parser.iterator().hasNext()) {
                throw new IllegalArgumentException("The CSV file has no header row.");
            }

            boolean headerSkipped = false;
            for (CSVRecord record : parser) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                if (columnIndex >= record.size()) {
                    throw new IllegalArgumentException("A data row is missing the selected column.");
                }
                String text = record.get(columnIndex).trim();
                if (text.isEmpty()) {
                    continue;
                }
                try {
                    double value = Double.parseDouble(text);
                    if (!Double.isFinite(value)) {
                        throw new NumberFormatException();
                    }
                    values.add(value);
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException(
                            "The selected column contains a non-numeric value: " + text, exception);
                }
            }
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("The selected column contains no numeric data.");
        }
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
