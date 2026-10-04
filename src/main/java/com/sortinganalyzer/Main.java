package com.sortinganalyzer;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load splash screen
        Parent splashRoot = FXMLLoader.load(
                getClass().getResource("/fxml/splash.fxml")
        );

        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.TRANSPARENT);

        Scene splashScene = new Scene(splashRoot, 300, 350);
        splashScene.setFill(Color.TRANSPARENT);

        splashStage.setScene(splashScene);
        splashStage.show();
        splashStage.centerOnScreen();

        // Show splash for 1.5 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

        pause.setOnFinished(event -> {
            try {
                Parent mainRoot = FXMLLoader.load(
                        getClass().getResource("/fxml/file-selection.fxml")
                );

                Stage mainStage = new Stage();

                Scene mainScene = new Scene(mainRoot, 900, 600);

                // Apply css
                mainScene.getStylesheets().add(
                        getClass().getResource("/css/style.css").toExternalForm()
                );

                mainStage.setScene(mainScene);
                mainStage.setTitle("Sorting Algorithm Performance Analyzer");
                mainStage.getIcons().add(
                        new Image(getClass().getResourceAsStream("/images/sortlab-logo.png"))
                );
                mainStage.setMaximized(true);
                mainStage.show();
                mainStage.centerOnScreen();

                splashStage.close();

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