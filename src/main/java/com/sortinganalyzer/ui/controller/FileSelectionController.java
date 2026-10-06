package com.sortinganalyzer.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Objects;
import javafx.scene.text.Text;

public class FileSelectionController implements WizardStep {

    @FXML private VBox emptyState;
    @FXML private VBox selectedState;
    @FXML private Label fileNameLabel;
    @FXML private Label fileSizeLabel;

    private final ObjectProperty<File> selectedFile = WizardState.get().selectedFileProperty();
    private final Tooltip fileNameTooltip = new Tooltip();

    @FXML
    private void initialize() {
        // show exactly one state at a time
        emptyState.visibleProperty().bind(selectedFile.isNull());
        emptyState.managedProperty().bind(emptyState.visibleProperty());

        selectedState.visibleProperty().bind(selectedFile.isNotNull());
        selectedState.managedProperty().bind(selectedState.visibleProperty());

        // labels follow the file, including when the screen is reloaded
        fileNameLabel.textProperty().bind(Bindings.createStringBinding(
                () -> selectedFile.get() == null ? "" : selectedFile.get().getName(),
                selectedFile));

        fileSizeLabel.textProperty().bind(Bindings.createStringBinding(
                () -> selectedFile.get() == null ? "" : formatSize(selectedFile.get().length()),
                selectedFile));

        // tooltip: wraps long text and always shows the full file name
        fileNameTooltip.setWrapText(true);
        fileNameTooltip.setMaxWidth(400);
        fileNameTooltip.textProperty().bind(fileNameLabel.textProperty());

        // re-check whenever the name or the font changes
        fileNameLabel.textProperty().addListener((obs, old, text) -> updateFileNameTooltip());
        fileNameLabel.fontProperty().addListener((obs, old, font) -> updateFileNameTooltip());
        updateFileNameTooltip();
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
            setFile(file);
        }
    }

    @FXML
    private void handleRemove() {
        setFile(null);
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
                setFile(file);
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private void setFile(File file) {
        if (!Objects.equals(selectedFile.get(), file)) {
            WizardState.get().selectedColumnProperty().set(null);
        }
        selectedFile.set(file);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void updateFileNameTooltip() {
        String name = fileNameLabel.getText();

        if (name == null || name.isEmpty()) {
            fileNameLabel.setTooltip(null);
            return;
        }

        Text measure = new Text(name);
        measure.setFont(fileNameLabel.getFont());
        double textWidth = measure.getLayoutBounds().getWidth();

        boolean truncated = textWidth > fileNameLabel.getMaxWidth();
        fileNameLabel.setTooltip(truncated ? fileNameTooltip : null);
    }
}