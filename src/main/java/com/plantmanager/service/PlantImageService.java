package com.plantmanager.service;

import com.plantmanager.model.Plant;
import com.plantmanager.model.PlantIcon;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads built-in icons and manages custom plant photos on disk.
 * CSV stores the image reference (path/key), not the binary image data.
 */
public final class PlantImageService {

    public static final Path IMAGE_DIR = Paths.get("plant-images");
    private static final int ICON_SIZE = 64;
    private static final Map<String, Image> CACHE = new HashMap<>();

    private PlantImageService() {
    }

    public static Image getImage(Plant plant) {
        String ref = plant.getImageRef();
        if (ref == null || ref.isBlank()) {
            return getBuiltinIcon(PlantIcon.defaultForType(plant.getPlantType()));
        }
        if (ref.startsWith("builtin:")) {
            PlantIcon icon = PlantIcon.fromKey(ref);
            return getBuiltinIcon(icon != null ? icon : PlantIcon.defaultForType(plant.getPlantType()));
        }
        if (ref.startsWith("file:")) {
            Path path = Paths.get(ref.substring(5));
            if (Files.exists(path)) {
                return loadFileImage(path);
            }
        }
        return getBuiltinIcon(PlantIcon.defaultForType(plant.getPlantType()));
    }

    public static Image getBuiltinIcon(PlantIcon icon) {
        return CACHE.computeIfAbsent(icon.getKey(), k -> generateIcon(icon));
    }

    public static Image previewFile(Path path) {
        if (path != null && Files.exists(path)) {
            return loadFileImage(path);
        }
        return null;
    }

    public static String saveCustomImage(int plantId, String plantName, Path sourceFile) throws IOException {
        Files.createDirectories(IMAGE_DIR);
        String ext = getExtension(sourceFile);
        String safeName = plantName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
        Path dest = IMAGE_DIR.resolve(plantId + "_" + safeName + ext);
        Files.copy(sourceFile, dest, StandardCopyOption.REPLACE_EXISTING);
        return "file:" + dest.toString();
    }

    public static String resolveImageRef(String imageRef, String plantType) {
        if (imageRef == null || imageRef.isBlank()) {
            return PlantIcon.defaultForType(plantType).getKey();
        }
        return imageRef.trim();
    }

    private static Image loadFileImage(Path path) {
        String uri = path.toUri().toString();
        String cacheKey = "file:" + uri;
        return CACHE.computeIfAbsent(cacheKey, k ->
                new Image(uri, ICON_SIZE, ICON_SIZE, true, true, true));
    }

    private static Image generateIcon(PlantIcon icon) {
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(ICON_SIZE, ICON_SIZE);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Rounded rectangle background
        gc.setFill(Color.web(icon.getColor()));
        gc.fillRoundRect(0, 0, ICON_SIZE, ICON_SIZE, 14, 14);

        // Emoji centered
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(28));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(icon.getEmoji(), ICON_SIZE / 2.0, ICON_SIZE / 2.0);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private static String getExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return name.substring(dot).toLowerCase();
        }
        return ".jpg";
    }
}
