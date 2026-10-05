package com.sortinganalyzer.ui.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class FileSelectionController implements WizardStep {

    @FXML private VBox emptyState;
    @FXML private VBox selectedState;
    @FXML private Label fileNameLabel;
    @FXML private Label fileSizeLabel;

    private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();

    @FXML
    private void initialize() {
        // show exactly one state at a time
        emptyState.visibleProperty().bind(selectedFile.isNull());
        emptyState.managedProperty().bind(emptyState.visibleProperty());

        selectedState.visibleProperty().bind(selectedFile.isNotNull());
        selectedState.managedProperty().bind(selectedState.visibleProperty());

        // fill in the details whenever the file changes
        selectedFile.addListener((obs, old, file) -> {
            if (file != null) {
                fileNameLabel.setText(file.getName());
                fileSizeLabel.setText(formatSize(file.length()));
            }
        });
    }

    // ---------- WizardStep ----------

    @Override
    public ObservableBooleanValue canProceed() {
        return selectedFile.isNotNull();
    }

    // ---------- FXML handlers ----------

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        Node source = (Node) event.getSource();
        File file = chooser.showOpenDialog(source.getScene().getWindow());

        if (file != null) {
            selectedFile.set(file);
        }
    }

    @FXML
    private void handleRemove() {
        selectedFile.set(null);
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        boolean success = false;

        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().getFirst();

            if (file.getName().toLowerCase().endsWith(".csv")) {
                selectedFile.set(file);
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    public File getSelectedFile() {
        return selectedFile.get();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}