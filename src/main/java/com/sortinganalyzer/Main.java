package com.sortinganalyzer;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Load splash screen
        Parent splashRoot = FXMLLoader.load(
                getClass().getResource("/fxml/splash.fxml")
        );

        Scene splashScene = new Scene(splashRoot, 300, 350);

        stage.setTitle("Sorting Algorithm Performance Analyzer");
        stage.setScene(splashScene);
        stage.show();

        // Show splash for 2 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(10));

        pause.setOnFinished(event -> {
            try {
                Parent mainRoot = FXMLLoader.load(
                        getClass().getResource("/fxml/file-selection.fxml")
                );

                Scene mainScene = new Scene(mainRoot, 900, 600);

                stage.setScene(mainScene);
                stage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        pause.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}