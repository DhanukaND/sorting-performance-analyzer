package com.sortinganalyzer.ui.controller;

import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class RunAlgorithmsController implements WizardStep {

    @FXML
    private ComboBox<Integer> runCountBox;

    @FXML
    private void initialize() {
        runCountBox.setItems(FXCollections.observableArrayList(1, 2, 3));
        runCountBox.getSelectionModel().select(WizardState.get().runCountProperty().get() - 1);
        runCountBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                WizardState.get().runCountProperty().set(newValue);
            }
        });
    }

    @Override
    public ObservableBooleanValue canProceed() {
        return runCountBox.getSelectionModel().selectedItemProperty().isNotNull();
    }
}
