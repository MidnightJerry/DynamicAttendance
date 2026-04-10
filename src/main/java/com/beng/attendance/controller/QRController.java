package com.beng.attendance.controller;

import com.beng.attendance.service.APIService;
import com.beng.attendance.util.QRGenerator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.File;

public class QRController {

    private final APIService apiService;
    private final QRGenerator qrGenerator;
    private final ImageView qrImageView;
    private final Label statusLabel;
    private final Label codeLabel;
    private final Label titleLabel;
    private final Label countdownLabel;
    private final VBox view;
    private Timeline updateTimeline;
    private Timeline countdownTimeline;
    private String currentCode = "";
    private int secondsRemaining = 7;
    private double fontSize = 12;
    private boolean isUpdating = true;  // Start with updates enabled
    private Button settingsButton;

    public QRController() {
        this.apiService = new APIService();
        this.qrGenerator = new QRGenerator();
        this.qrImageView = new ImageView();
        this.statusLabel = new Label("Starting...");
        this.codeLabel = new Label("");
        this.countdownLabel = new Label("Next update in: 7s");

        // Setup layout
        this.view = new VBox(15);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(20));

        // Load background
        loadBackground();

        // Title
        titleLabel = new Label("↓ COPY ATTENDANCE CODE ↓");
        titleLabel.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #2c3e50;
            -fx-font-family: 'Arial';
        """);

        // Subtitle
        Label subtitleLabel = new Label("Dynamic Code");
        subtitleLabel.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #7f8c8d;
            -fx-padding: 0 0 10 0;
        """);

        // QR code styling
        qrImageView.setFitWidth(220);
        qrImageView.setFitHeight(220);
        qrImageView.setPreserveRatio(true);
        qrImageView.setStyle("""
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);
            -fx-background-color: white;
            -fx-padding: 10;
            -fx-background-radius: 10;
        """);

        // Show loading placeholder in QR area
        Label qrPlaceholder = new Label("Loading QR...");
        qrPlaceholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        qrImageView.setImage(null);

        // Code label
        codeLabel.setStyle(String.format("""
            -fx-font-size: %.0fpx;
            -fx-font-weight: bold;
            -fx-text-fill: #3498db;
            -fx-font-family: 'Courier New';
            -fx-padding: 10 0 0 0;
        """, fontSize));

        // Countdown label
        countdownLabel.setStyle("""
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-text-fill: #e74c3c;
            -fx-padding: 5 0 0 0;
        """);

        // Status label
        statusLabel.setStyle(String.format("""
            -fx-font-size: %.0fpx;
            -fx-text-fill: #95a5a6;
            -fx-padding: 5 0 0 0;
        """, fontSize));

        // Settings button
        settingsButton = new Button("⚙️ Settings");
        settingsButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-padding: 5 10 5 10;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        settingsButton.setOnAction(e -> showSettingsDialog());

        settingsButton.setOnMouseEntered(e ->
                settingsButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10 5 10; -fx-background-radius: 5; -fx-cursor: hand;")
        );
        settingsButton.setOnMouseExited(e ->
                settingsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10 5 10; -fx-background-radius: 5; -fx-cursor: hand;")
        );

        // Decorative line
        Label line = new Label("━━━━━━━━━━━━━━━━");
        line.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10px;");

        // Add all to view
        view.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                qrImageView,
                codeLabel,
                countdownLabel,
                statusLabel,
                settingsButton,
                line
        );

        // Setup drag and drop for background
        setupDragAndDrop();
    }

    private void loadBackground() {
        try {
            File customBg = new File("background.png");
            if (customBg.exists()) {
                Image backgroundImage = new Image(customBg.toURI().toString());
                BackgroundImage bgImage = new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                view.setBackground(new Background(bgImage));
            } else {
                try {
                    Image backgroundImage = new Image(getClass().getResourceAsStream("/images/background.png"));
                    BackgroundImage bgImage = new BackgroundImage(
                            backgroundImage,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                    );
                    view.setBackground(new Background(bgImage));
                } catch (Exception e) {
                    view.setStyle("-fx-background-color: linear-gradient(#667eea, #764ba2);");
                }
            }
        } catch (Exception e) {
            view.setStyle("-fx-background-color: linear-gradient(#667eea, #764ba2);");
        }
    }

    private void setupDragAndDrop() {
        view.setOnDragOver(event -> {
            event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            event.consume();
        });

        view.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") ||
                        fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                    try {
                        Image backgroundImage = new Image(file.toURI().toString());
                        BackgroundImage bgImage = new BackgroundImage(
                                backgroundImage,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                        );
                        view.setBackground(new Background(bgImage));
                        statusLabel.setText("✓ Background changed");
                        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                    } catch (Exception e) {
                        statusLabel.setText("❌ Failed to load image");
                    }
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    private void showSettingsDialog() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initStyle(StageStyle.UTILITY);
        settingsStage.setTitle("Settings");
        settingsStage.setResizable(false);

        VBox settingsLayout = new VBox(15);
        settingsLayout.setPadding(new Insets(20));
        settingsLayout.setAlignment(Pos.CENTER);

        Label fontLabel = new Label("Font Size: " + (int)fontSize + "px");
        fontLabel.setStyle("-fx-font-size: 12px;");

        Slider fontSlider = new Slider(8, 30, fontSize);
        fontSlider.setShowTickLabels(true);
        fontSlider.setShowTickMarks(true);
        fontSlider.setMajorTickUnit(4);
        fontSlider.setBlockIncrement(1);

        fontSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            fontSize = newVal.intValue();
            fontLabel.setText("Font Size: " + (int)fontSize + "px");
            updateFontSize();
        });

        Button toggleUpdateButton = new Button(isUpdating ? "⏸ Stop Updates" : "▶️ Start Updates");
        toggleUpdateButton.setStyle(isUpdating ? """
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-padding: 8 15 8 15;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            """ : """
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-padding: 8 15 8 15;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            """);

        toggleUpdateButton.setOnAction(e -> {
            if (isUpdating) {
                stopUpdating();
                toggleUpdateButton.setText("▶️ Start Updates");
                toggleUpdateButton.setStyle("""
                    -fx-background-color: #27ae60;
                    -fx-text-fill: white;
                    -fx-font-size: 12px;
                    -fx-padding: 8 15 8 15;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """);
                statusLabel.setText("⏸ Updates paused");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                countdownLabel.setText("Updates paused");
            } else {
                startUpdating();
                toggleUpdateButton.setText("⏸ Stop Updates");
                toggleUpdateButton.setStyle("""
                    -fx-background-color: #e74c3c;
                    -fx-text-fill: white;
                    -fx-font-size: 12px;
                    -fx-padding: 8 15 8 15;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """);
                statusLabel.setText("✓ Updates resumed");
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
            }
        });

        Button chooseBgButton = new Button("🖼️ Choose Background");
        chooseBgButton.setStyle("""
            -fx-background-color: #9b59b6;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-padding: 5 10 5 10;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);

        chooseBgButton.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Background Image");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(settingsStage);
            if (selectedFile != null) {
                try {
                    Image backgroundImage = new Image(selectedFile.toURI().toString());
                    BackgroundImage bgImage = new BackgroundImage(
                            backgroundImage,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                    );
                    view.setBackground(new Background(bgImage));
                    statusLabel.setText("✓ Background changed");
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                } catch (Exception ex) {
                    statusLabel.setText("❌ Failed to load background");
                }
            }
        });

        Button resetButton = new Button("⟳ Reset to Default");
        resetButton.setStyle("""
            -fx-background-color: #95a5a6;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-padding: 5 10 5 10;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);

        resetButton.setOnAction(e -> {
            fontSize = 12;
            fontSlider.setValue(12);
            updateFontSize();
            loadBackground();
            if (!isUpdating) {
                toggleUpdateButton.fire();
            }
            statusLabel.setText("✓ Reset to default");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-padding: 5 15 5 15;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        closeButton.setOnAction(e -> settingsStage.close());

        settingsLayout.getChildren().addAll(
                fontLabel, fontSlider,
                toggleUpdateButton,
                chooseBgButton,
                resetButton,
                closeButton
        );

        Scene scene = new Scene(settingsLayout, 280, 380);
        settingsStage.setScene(scene);
        settingsStage.showAndWait();
    }

    private void updateFontSize() {
        codeLabel.setStyle(String.format("""
            -fx-font-size: %.0fpx;
            -fx-font-weight: bold;
            -fx-text-fill: #3498db;
            -fx-font-family: 'Courier New';
            -fx-padding: 10 0 0 0;
        """, fontSize));

        statusLabel.setStyle(String.format("""
            -fx-font-size: %.0fpx;
            -fx-text-fill: #95a5a6;
            -fx-padding: 5 0 0 0;
        """, fontSize));

        titleLabel.setStyle(String.format("""
            -fx-font-size: %.0fpx;
            -fx-font-weight: bold;
            -fx-text-fill: #2c3e50;
            -fx-font-family: 'Arial';
        """, fontSize));
    }

    public VBox getView() {
        return view;
    }

    public void startUpdating() {
        System.out.println("startUpdating() called - isUpdating=" + isUpdating);

        // Allow restart even if already updating
        if (isUpdating) {
            System.out.println("Already updating, stopping first");
            stopUpdating();
        }

        System.out.println("Starting updates now");
        isUpdating = true;

        // Reset seconds remaining
        secondsRemaining = 7;

        // Create new timelines
        updateTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateQRCode()),
                new KeyFrame(Duration.seconds(7))
        );
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateCountdown()),
                new KeyFrame(Duration.seconds(1))
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    public void stopUpdating() {
        if (!isUpdating) return;

        isUpdating = false;

        if (updateTimeline != null) {
            updateTimeline.stop();
            updateTimeline = null;
        }
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void updateCountdown() {
        if (!isUpdating) return;

        if (secondsRemaining > 0) {
            secondsRemaining--;
            countdownLabel.setText("⏱️ Next update in: " + secondsRemaining + "s");
        }
    }

    private void updateQRCode() {
        if (!isUpdating) return;

        try {
            statusLabel.setText("⟳ Fetching code...");
            statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");

            String attendanceCode = apiService.fetchAttendanceCode();

            if (attendanceCode != null && !attendanceCode.isEmpty()) {
                currentCode = attendanceCode;
                Image qrImage = qrGenerator.generateQRCode(attendanceCode, 220, 220);
                qrImageView.setImage(qrImage);
                codeLabel.setText("📱 " + attendanceCode);
                statusLabel.setText("✓ Updated");
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                secondsRemaining = 7;
                countdownLabel.setText("⏱️ Next update in: 7s");
            } else {
                statusLabel.setText("⚠️ API error | Retrying...");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            }
        } catch (Exception e) {
            statusLabel.setText("❌ Connection error");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            e.printStackTrace();
        }
    }
}