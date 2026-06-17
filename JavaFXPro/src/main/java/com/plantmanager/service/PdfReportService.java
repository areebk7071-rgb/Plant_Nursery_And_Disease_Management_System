package com.plantmanager.service;

import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.Plant;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates a printable Garden Health Report as PDF.
 */
public final class PdfReportService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float LINE_HEIGHT = 14;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private PdfReportService() {
    }

    public static void exportGardenReport(List<Plant> plants, Path destination) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PageWriter writer = new PageWriter(document);
            writeTitlePage(writer, plants);
            writePlantCatalog(writer, plants);
            writeTreatmentSection(writer, plants);
            writeDiseaseLibrarySummary(writer);
            writer.close();
            document.save(destination.toFile());
        }
    }

    private static void writeTitlePage(PageWriter w, List<Plant> plants) throws IOException {
        w.drawTitle("Garden Health Report");
        w.drawSubtitle("Botanical Treatment Advisor");
        w.drawText("Generated: " + LocalDateTime.now().format(DATE_FMT), 12, false);
        w.space(20);

        int total = plants.size();
        int healthy = DashboardStats.countHealthy(plants);
        int diseased = DashboardStats.countDiseased(plants);
        double score = DashboardStats.healthPercentage(plants);

        w.drawSection("Summary");
        w.drawText("Total plants: " + total, 11, false);
        w.drawText("Healthy: " + healthy, 11, false);
        w.drawText("Needs treatment: " + diseased, 11, false);
        w.drawText(String.format("Health score: %.0f%%", score), 11, true);
        w.space(8);

        w.drawSection("Plants by Type");
        for (Map.Entry<String, Long> entry : DashboardStats.countByType(plants).entrySet()) {
            w.drawText("  " + entry.getKey() + ": " + entry.getValue(), 11, false);
        }
        w.space(8);

        Map<String, Long> byDisease = DashboardStats.countByDisease(plants);
        if (!byDisease.isEmpty()) {
            w.drawSection("Disease Overview");
            var sorted = byDisease.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .toList();
            for (var e : sorted) {
                w.drawText("  " + e.getKey() + ": " + e.getValue() + " plant(s)", 11, false);
            }
        }
    }

    private static void writePlantCatalog(PageWriter w, List<Plant> plants) throws IOException {
        w.ensureSpace(60);
        w.drawSection("Plant Catalog");

        float[] colX = {MARGIN, MARGIN + 30, MARGIN + 130, MARGIN + 280, MARGIN + 340, MARGIN + 420};
        String[] headers = {"ID", "Name", "Species", "Type", "Health", "Disease"};

        w.drawTableHeader(headers, colX);
        for (Plant plant : plants) {
            w.ensureSpace(LINE_HEIGHT + 4);
            w.drawTableRow(new String[]{
                    String.valueOf(plant.getId()),
                    truncate(plant.getName(), 18),
                    truncate(plant.getSpecies(), 22),
                    plant.getPlantType(),
                    plant.getHealthStatus(),
                    truncate(plant.getAssignedDiseaseName(), 16)
            }, colX);
        }
    }

    private static void writeTreatmentSection(PageWriter w, List<Plant> plants) throws IOException {
        List<Plant> diseased = plants.stream().filter(Plant::hasDisease).toList();
        if (diseased.isEmpty()) {
            w.ensureSpace(40);
            w.drawSection("Treatment Plans");
            w.drawText("No diseased plants — no treatments required.", 11, false);
            return;
        }

        w.ensureSpace(40);
        w.drawSection("Treatment Plans");

        for (Plant plant : diseased) {
            Disease disease = plant.getAssignedDisease();
            w.ensureSpace(80);
            w.drawText(plant.getName() + " (" + plant.getPlantType() + ")", 12, true);
            w.drawText("Disease: " + disease.getName(), 11, false);
            w.drawText("Symptoms: " + disease.getSymptoms(), 10, false);
            w.drawText("Agent: " + disease.getCausativeAgent(), 10, false);
            w.space(4);
            w.drawText("Chemical: " + disease.getTreatment().getChemicalName()
                    + " — " + disease.getTreatment().getApplicationMethod(), 10, false);
            w.drawText("Organic: " + disease.getTreatment().getOrganicAlternative(), 10, false);
            int days = disease.getTreatment().getDurationDays();
            w.drawText("Duration: " + (days > 0 ? days + " days" : "Ongoing"), 10, false);
            if (!disease.getTreatment().getPrecautions().isBlank()) {
                w.drawText("Precautions: " + disease.getTreatment().getPrecautions(), 10, false);
            }
            w.space(10);
        }
    }

    private static void writeDiseaseLibrarySummary(PageWriter w) throws IOException {
        w.ensureSpace(50);
        w.drawSection("Disease Library (" + DiseaseLibrary.getAll().size() + " entries)");
        for (var disease : DiseaseLibrary.getAll()) {
            w.ensureSpace(LINE_HEIGHT + 2);
            w.drawText("• " + disease.getName() + " — " + disease.getCausativeAgent(), 10, false);
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 2) + "..";
    }

    private static class PageWriter {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;
        private int pageNumber;

        PageWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void newPage() throws IOException {
            closeStream();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            pageNumber++;
            y = PAGE_HEIGHT - MARGIN;

            stream.setNonStrokingColor(0.11f, 0.26f, 0.20f);
            stream.beginText();
            stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            stream.newLineAtOffset(MARGIN, 30);
            stream.showText("Garden Health Report  •  Page " + pageNumber);
            stream.endText();
            stream.setNonStrokingColor(0, 0, 0);
        }

        void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN + 40) {
                newPage();
            }
        }

        void drawTitle(String text) throws IOException {
            drawText(text, 20, true);
            y -= 6;
        }

        void drawSubtitle(String text) throws IOException {
            stream.setNonStrokingColor(0.2f, 0.4f, 0.3f);
            drawText(text, 12, false);
            stream.setNonStrokingColor(0, 0, 0);
            y -= 4;
        }

        void drawSection(String text) throws IOException {
            ensureSpace(30);
            y -= 8;
            stream.setNonStrokingColor(0.11f, 0.26f, 0.20f);
            drawText(text, 14, true);
            stream.setNonStrokingColor(0, 0, 0);
            y -= 2;
            stream.setStrokingColor(0.32f, 0.55f, 0.35f);
            stream.moveTo(MARGIN, y);
            stream.lineTo(PAGE_WIDTH - MARGIN, y);
            stream.stroke();
            y -= 10;
        }

        void drawText(String text, float size, boolean bold) throws IOException {
            ensureSpace(LINE_HEIGHT);
            stream.beginText();
            stream.setFont(new PDType1Font(bold
                    ? Standard14Fonts.FontName.HELVETICA_BOLD
                    : Standard14Fonts.FontName.HELVETICA), size);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(sanitize(text));
            stream.endText();
            y -= LINE_HEIGHT;
        }

        void drawTableHeader(String[] headers, float[] colX) throws IOException {
            ensureSpace(LINE_HEIGHT + 6);
            stream.setNonStrokingColor(0.9f, 0.95f, 0.9f);
            stream.addRect(MARGIN, y - 4, PAGE_WIDTH - 2 * MARGIN, LINE_HEIGHT + 4);
            stream.fill();
            stream.setNonStrokingColor(0.11f, 0.26f, 0.20f);

            for (int i = 0; i < headers.length; i++) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                stream.newLineAtOffset(colX[i], y);
                stream.showText(headers[i]);
                stream.endText();
            }
            stream.setNonStrokingColor(0, 0, 0);
            y -= LINE_HEIGHT + 6;
        }

        void drawTableRow(String[] cells, float[] colX) throws IOException {
            for (int i = 0; i < cells.length; i++) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                stream.newLineAtOffset(colX[i], y);
                stream.showText(sanitize(cells[i]));
                stream.endText();
            }
            y -= LINE_HEIGHT + 2;
        }

        void space(float amount) {
            y -= amount;
        }

        void close() throws IOException {
            closeStream();
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        private String sanitize(String text) {
            if (text == null) {
                return "";
            }
            return text.replace('\n', ' ').replace('\r', ' ')
                    .replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
}
