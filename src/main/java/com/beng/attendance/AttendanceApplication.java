package com.beng.attendance;

import com.beng.attendance.controller.QRController;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AttendanceApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create controller
        QRController controller = new QRController();

        // Setup UI
        VBox root = controller.getView();
        root.setStyle("-fx-background-color: white; -fx-padding: 10;");

        Scene scene = new Scene(root, 300, 500); // Narrow vertical bar

        // Window settings
        primaryStage.setTitle("Attendance QR");
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setOpacity(0.7);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);

        // Position on right side of screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getWidth() - 320);
        primaryStage.setY(100);

        primaryStage.show();

        // Start updating QR codes
        controller.startUpdating();
    }

    @Override
    public void stop() {
        // Cleanup when app closes
        System.exit(0);
    }
}