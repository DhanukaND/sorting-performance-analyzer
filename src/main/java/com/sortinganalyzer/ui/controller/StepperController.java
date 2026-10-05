package com.sortinganalyzer.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

import java.util.List;

public class StepperController {

    @FXML private Circle circle1, circle2, circle3, circle4;
    @FXML private Label number1, number2, number3, number4;
    @FXML private Label label1, label2, label3, label4;

    private List<Circle> circles;
    private List<Label> numbers;
    private List<Label> labels;

    @FXML
    private void initialize() {
        circles = List.of(circle1, circle2, circle3, circle4);
        numbers = List.of(number1, number2, number3, number4);
        labels  = List.of(label1, label2, label3, label4);
        setCurrentStep(1);
    }

    /** step is 1-based: 1 = Select CSV File ... 4 = View Results */
    public void setCurrentStep(int step) {
        for (int i = 0; i < circles.size(); i++) {
            int stepNo = i + 1;

            String suffix;
            if (stepNo == step) {
                suffix = "-active";
            } else if (stepNo < step) {
                suffix = "-done";
            } else {
                suffix = "";   // upcoming
            }

            circles.get(i).getStyleClass().setAll("step-circle" + suffix);

            // keep Label's default "label" class so default label styling isn't lost
            numbers.get(i).getStyleClass().setAll("label", "step-number" + suffix);
            labels.get(i).getStyleClass().setAll("label", "step-label" + suffix);

            // active circle is 20; inactive and done have a 2px stroke, so 19 keeps them the same visual size
            circles.get(i).setRadius(stepNo == step ? 20 : 19);
        }
    }
}