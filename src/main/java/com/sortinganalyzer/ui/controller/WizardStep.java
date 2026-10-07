package com.sortinganalyzer.ui.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;

import java.util.List;

public interface WizardStep {

    /** Extra buttons shown in the middle of the bottom bar (Remove, Clear...). */
    default List<Node> getExtraButtons() { return List.of(); }

    /** Next is enabled only while this is true. */
    default ObservableBooleanValue canProceed() { return new SimpleBooleanProperty(true); }

    /** Text of the Next button for this screen. */
    default String getNextText() { return "Next"; }

    /** Called when Next is clicked. Return false to block moving on. */
    default boolean onNext() { return true; }
}