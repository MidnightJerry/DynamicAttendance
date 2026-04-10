package com.beng.attendance.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;

public class QRGenerator {

    public Image generateQRCode(String text, int width, int height) {
        try {
            // Generate QR code matrix
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            // Convert to BufferedImage
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Convert to JavaFX Image
            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (Exception e) {
            System.err.println("QR Generation Error: " + e.getMessage());
            return null;
        }
    }
}
