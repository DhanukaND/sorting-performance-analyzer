package com.sortinganalyzer.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainLayoutController {

    // Injected from <fx:include fx:id="stepper">  ->  "stepper" + "Controller"
    @FXML private StepperController stepperController;

    @FXML private StackPane contentHolder;
    @FXML private HBox extraActions;
    @FXML private Button backButton;
    @FXML private Button nextButton;

    private static final String[] SCREENS = {
            "/fxml/file-selection.fxml",
            "/fxml/column-selection.fxml",
            "/fxml/run-algorithms.fxml",
            "/fxml/results.fxml"
    };

    private int index = 0;
    private WizardStep currentStep;

    @FXML
    private void initialize() {
        show(0);
    }

    private void show(int newIndex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SCREENS[newIndex]));
            Node content = loader.load();
            contentHolder.getChildren().setAll(content);

            index = newIndex;
            stepperController.setCurrentStep(index + 1);

            // reset the nav bar to defaults
            nextButton.disableProperty().unbind();
            nextButton.setDisable(false);
            nextButton.setText(index == SCREENS.length - 1 ? "Finish" : "Next");
            extraActions.getChildren().clear();
            backButton.setVisible(index > 0);
            backButton.setManaged(index > 0);

            // let the screen customise it
            Object controller = loader.getController();
            if (controller instanceof WizardStep step) {
                currentStep = step;
                extraActions.getChildren().setAll(step.getExtraButtons());
                nextButton.setText(step.getNextText());
                nextButton.disableProperty().bind(Bindings.not(step.canProceed()));
            } else {
                currentStep = null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + SCREENS[newIndex], e);
        }
    }

    @FXML
    private void handleNext() {
        if (currentStep != null && !currentStep.onNext()) return;
        if (index < SCREENS.length - 1) show(index + 1);
    }

    @FXML
    private void handleBack() {
        if (index > 0) show(index - 1);
    }
}
