package com.plantmanager.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.concurrent.Executors;

/**
 * Serves a file over local HTTP so a QR code can link to it for phone download.
 * Works when the phone is on the same Wi-Fi network as this computer.
 */
public final class LocalFileShareServer {

    private final HttpServer server;
    private final String downloadUrl;
    private final Path file;
    private final String fileName;
    private final String contentType;

    private LocalFileShareServer(HttpServer server, String downloadUrl, Path file,
                                 String fileName, String contentType) {
        this.server = server;
        this.downloadUrl = downloadUrl;
        this.file = file;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public static LocalFileShareServer start(Path file, String downloadFileName) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("File not found: " + file);
        }

        String contentType = downloadFileName.toLowerCase().endsWith(".pdf")
                ? "application/pdf"
                : "text/csv; charset=UTF-8";

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/download", exchange -> handleDownload(exchange, file, downloadFileName, contentType));

        server.start();

        String ip = findLocalIpAddress();
        int port = server.getAddress().getPort();
        String url = "http://" + ip + ":" + port + "/download";

        return new LocalFileShareServer(server, url, file, downloadFileName, contentType);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void stop() {
        server.stop(0);
    }

    public static Image generateQrCode(String url, int size) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, size, size);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", png);
        return new Image(new ByteArrayInputStream(png.toByteArray()));
    }

    private static void handleDownload(HttpExchange exchange, Path file, String fileName, String contentType)
            throws IOException {
        byte[] data = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Disposition",
                "attachment; filename=\"" + fileName.replace("\"", "") + "\"");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    static String findLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
