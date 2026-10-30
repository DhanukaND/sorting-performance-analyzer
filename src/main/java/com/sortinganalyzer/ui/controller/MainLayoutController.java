package com.sortinganalyzer.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

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

    public void show(int newIndex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SCREENS[newIndex]));
            Node content = loader.load();
            contentHolder.getChildren().setAll(content);

            index = newIndex;
            stepperController.setCurrentStep(index + 1);

            nextButton.disableProperty().unbind();
            nextButton.setDisable(false);
            nextButton.setText(index == SCREENS.length - 1 ? "Finish" : "Next");
            setNextIcon("fas-arrow-right");          // reset to the default arrow
            extraActions.getChildren().clear();
            backButton.setVisible(index > 0);
            backButton.setManaged(index > 0);

            Object controller = loader.getController();
            if (controller instanceof WizardStep step) {
                currentStep = step;
                extraActions.getChildren().setAll(step.getExtraButtons());
                nextButton.setText(step.getNextText());
                setNextIcon(step.getNextIcon());
                nextButton.disableProperty().bind(Bindings.not(step.canProceed()));
            } else {
                currentStep = null;
            }

            if (controller instanceof RunAlgorithmsController runController) {
                runController.setNavigationCallback(() -> {
                    if (index < SCREENS.length - 1) {
                        show(index + 1);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + SCREENS[newIndex], e);
        }
    }

    private void setNextIcon(String iconLiteral) {
        if (nextButton.getGraphic() instanceof FontIcon icon) {
            icon.setIconLiteral(iconLiteral);
        }
    }

    @FXML
    private void handleNext() {
        if (currentStep != null && !currentStep.onNext()) return;
        if (index < SCREENS.length - 1) {
            show(index + 1);
        } else {
            startNewAnalysis();   // last screen: the button restarts the wizard
        }
    }

    private void startNewAnalysis() {
        WizardState.get().reset();
        show(0);
    }

    @FXML
    private void handleBack() {
        if (index > 0) show(index - 1);
    }
}