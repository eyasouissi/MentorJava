package tn.esprit.controllers;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import tn.esprit.entities.Evenement;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrontEvenementDetailsController {

    private static final int QR_CODE_SIZE = 400;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Evenement currentEvenement;

    @FXML private Label titreLabel, dateLabel, heureLabel, lieuLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView qrCodeImageView;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button saveButton;

    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        saveButton.setDisable(true);
    }

    public void initData(Evenement evenement) {
        this.currentEvenement = evenement;
        titreLabel.setText(evenement.getTitreE());
        dateLabel.setText(formatDate(evenement.getDateDebut()));
        heureLabel.setText(formatTime(evenement.getDateDebut()));
        lieuLabel.setText(evenement.getLieu());
        descriptionArea.setText(evenement.getDescriptionE());
        generateQRCode();
    }

    private void generateQRCode() {
        progressIndicator.setVisible(true);

        executor.execute(() -> {
            try {
                String html = generateInvitationHtml();
                Image qrImage = generateQRCodeImage(html);

                Platform.runLater(() -> {
                    qrCodeImageView.setImage(qrImage);
                    progressIndicator.setVisible(false);
                    saveButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> progressIndicator.setVisible(false));
            }
        });
    }

    private String generateInvitationHtml() {
        return """
        <!DOCTYPE html>
        <html lang='fr'>
        <head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
        <title>Invitation</title></head>
        <body style='font-family:Arial;padding:20px;background:#f0f0f0'>
        <div style='background:#fff;padding:20px;border-radius:10px;max-width:400px;margin:auto'>
        <h2 style='color:#6A1B9A;text-align:center'>""" + escape(currentEvenement.getTitreE()) + "</h2>" +
                "<p><b>Date :</b> " + formatDate(currentEvenement.getDateDebut()) + "</p>" +
                "<p><b>Heure :</b> " + formatTime(currentEvenement.getDateDebut()) + "</p>" +
                "<p><b>Lieu :</b> " + escape(currentEvenement.getLieu()) + "</p>" +
                "<p>" + escape(currentEvenement.getDescriptionE()) + "</p></div></body></html>";
    }

    private Image generateQRCodeImage(String content) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        BufferedImage qrImage = new BufferedImage(QR_CODE_SIZE, QR_CODE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qrImage.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, QR_CODE_SIZE, QR_CODE_SIZE);
        g.setColor(new Color(106, 27, 154));
        for (int x = 0; x < QR_CODE_SIZE; x++) {
            for (int y = 0; y < QR_CODE_SIZE; y++) {
                if (matrix.get(x, y)) g.fillRect(x, y, 1, 1);
            }
        }
        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", os);
        return new Image(new ByteArrayInputStream(os.toByteArray()));
    }

    @FXML
    private void handleSaveQR(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer QR Code");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        chooser.setInitialFileName("qr_invitation.png");
        File file = chooser.showSaveDialog(((Button)event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                WritableImage writableImage = qrCodeImageView.snapshot(null, null);
                BufferedImage bufferedImage = new BufferedImage((int) writableImage.getWidth(), (int) writableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < writableImage.getWidth(); x++) {
                    for (int y = 0; y < writableImage.getHeight(); y++) {
                        javafx.scene.paint.Color fxColor = writableImage.getPixelReader().getColor(x, y);
                        int argb = ((int)(fxColor.getOpacity() * 255) << 24) |
                                ((int)(fxColor.getRed() * 255) << 16) |
                                ((int)(fxColor.getGreen() * 255) << 8) |
                                ((int)(fxColor.getBlue() * 255));
                        bufferedImage.setRGB(x, y, argb);
                    }
                }
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatDate(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
    }

    private String formatTime(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH));
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    @FXML
    private void handleClose(ActionEvent event) {
        executor.shutdownNow();
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
    }
}
