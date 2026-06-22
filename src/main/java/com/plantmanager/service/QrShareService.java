package com.plantmanager.service;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Shows a QR code dialog pointing to a temporary local download URL.
 * Database-backed PDF reports are generated from SQLite before sharing.
 */
public final class QrShareService {

    private static LocalFileShareServer activeServer;

    private QrShareService() {
    }

    public static void shareDatabasePdf(Path pdfFile, Stage owner) throws Exception {
        shareFile(pdfFile, "garden_health_report.pdf",
                "Live Database PDF Report",
                "This PDF was compiled from the current SQLite plant and disease records.",
                owner);
    }

    public static void shareFile(Path file, String downloadFileName, Stage owner) throws Exception {
        shareFile(file, downloadFileName, "Share via QR Code",
                "Scan with your phone camera. Your phone must be on the same Wi-Fi as this computer.",
                owner);
    }

    private static void shareFile(Path file, String downloadFileName, String dialogTitle,
                                  String hintText, Stage owner) throws Exception {
        stopActiveServer();
        activeServer = LocalFileShareServer.start(file, downloadFileName);
        showDialog(owner, activeServer.getDownloadUrl(), downloadFileName, dialogTitle, hintText);
    }

    public static void stopActiveServer() {
        if (activeServer != null) {
            activeServer.stop();
            activeServer = null;
        }
    }

    private static void showDialog(Stage owner, String url, String fileName,
                                   String dialogTitle, String hintText) throws Exception {
        Label title = new Label(dialogTitle + ": " + fileName);
        title.getStyleClass().add("section-title");

        Label hint = new Label(hintText);
        hint.getStyleClass().add("chart-hint");
        hint.setWrapText(true);

        ImageView qrView = new ImageView(LocalFileShareServer.generateQrCode(url, 260));
        qrView.setPreserveRatio(true);
        qrView.setFitWidth(260);
        qrView.setFitHeight(260);

        Label urlLabel = new Label(url);
        urlLabel.getStyleClass().add("field-label");
        urlLabel.setWrapText(true);

        Label note = new Label("The download link stops when you close this window.");
        note.getStyleClass().add("chart-hint");

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("primary-button");
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        VBox root = new VBox(14, title, hint, qrView, urlLabel, note, closeBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setPrefWidth(380);
        root.getStylesheets().add(
                QrShareService.class.getResource("/com/plantmanager/view/styles.css").toExternalForm());

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(dialogTitle);
        dialog.setScene(new Scene(root));
        dialog.setOnCloseRequest(e -> stopActiveServer());
        closeBtn.setOnAction(e -> {
            stopActiveServer();
            dialog.close();
        });
        dialog.showAndWait();
        stopActiveServer();
    }
}
