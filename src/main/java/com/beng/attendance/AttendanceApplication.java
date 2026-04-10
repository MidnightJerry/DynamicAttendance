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
        QRController controller = new QRController();
        VBox root = controller.getView();

        Scene scene = new Scene(root, 320, 600);

        primaryStage.setTitle("Attendance QR");
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setOpacity(0.7);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(true);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getWidth() - 340);
        primaryStage.setY(100);

        primaryStage.show();

        controller.startUpdating();
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}