package com.beng.attendance.controller;

import com.beng.attendance.service.APIService;
import com.beng.attendance.util.QRGenerator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class QRController {

    private final APIService apiService;
    private final QRGenerator qrGenerator;
    private final ImageView qrImageView;
    private final Label statusLabel;
    private final VBox view;
    private Timeline updateTimeline;

    public QRController() {
        this.apiService = new APIService();
        this.qrGenerator = new QRGenerator();
        this.qrImageView = new ImageView();
        this.statusLabel = new Label("Loading...");

        // Setup layout
        this.view = new VBox(10);
        view.getChildren().addAll(qrImageView, statusLabel);

        // Style QR image
        qrImageView.setFitWidth(250);
        qrImageView.setFitHeight(250);
        qrImageView.setPreserveRatio(true);

        // Style status label
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
    }

    public VBox getView() {
        return view;
    }

    public void startUpdating() {
        // Update every 7 seconds
        updateTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateQRCode()),
                new KeyFrame(Duration.seconds(7))
        );
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }

    private void updateQRCode() {
        try {
            statusLabel.setText("Fetching code...");

            // Get attendance code from API (change URL to your actual API)
            String attendanceCode = apiService.fetchAttendanceCode();

            if (attendanceCode != null && !attendanceCode.isEmpty()) {
                // Generate QR code
                Image qrImage = qrGenerator.generateQRCode(attendanceCode, 250, 250);
                qrImageView.setImage(qrImage);
                statusLabel.setText("Code: " + attendanceCode + " | Valid for 7s");
            } else {
                statusLabel.setText("Failed to get code. Retrying...");
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopUpdating() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
    }
}